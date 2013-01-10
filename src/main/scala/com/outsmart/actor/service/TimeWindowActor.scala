package com.outsmart.actor.service

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.util.duration._
import com.outsmart.measurement.{Interpolated, Measurement}
import com.outsmart.actor.write.WriteMasterActor
import com.outsmart.Settings

/**
  * @author Vadim Bobrov
  */
case object Tick
class TimeWindowActor(var expiredTimeWindow : Int = Settings.ExpiredTimeWindow) extends Actor with ActorLogging{

	import context._

	var measurements = List[Measurement]()

	var writeMaster = actorOf(Props(new WriteMasterActor()), name = "writeMaster")
	var interpolatorFactory  : (String, String, String) => ActorRef = DefaultInterpolatorFactory.get

	override def preStart() {
		super.preStart()

		system.scheduler.schedule(0 milliseconds, 1000 milliseconds, self, Tick)
	}

	protected def receive: Receive = {

		// save interpolated value
		case imsmt : Interpolated => writeMaster ! imsmt

		case msmt : Measurement => {

			//TODO instead of tagging measurement one can also use pattern matching on sender
			//TODO aggregation should be done by .... what???
			measurements ::= msmt

			processWindow
		}

		// clean window
		case Tick => processWindow

	}


	/**
	 * send events older than a time window for interpolation
	 * and remove them from window
	 */
	private def processWindow() {

		val current = System.currentTimeMillis()
		// if any of the existing measurements are more than 9.5 minutes old
		// sort by time, interpolate, save to storage and discard
		val oldmsmt = (measurements filter (current - _.timestamp > expiredTimeWindow)).sortWith(_ < _)

		for (tv <- oldmsmt) {
			//log.info("sending to interpolation")
			interpolatorFactory(tv.customer, tv.location, tv.wireid) ! tv
		}
		// discard old values
		measurements = measurements filter (current - _.timestamp <= expiredTimeWindow)

	}

	object DefaultInterpolatorFactory {
		var interpolators = Map[(String, String, String), ActorRef]()

		def get(customer : String, location : String, wireid : String) : ActorRef = {
			if (!interpolators.contains(customer, location, wireid)) {
				log.info("creating new interpolator for " + customer + " " + location + " " + wireid)
				interpolators += ((customer, location, wireid) -> actorOf(Props(new InterpolatorActor(100))))
			}

			interpolators(customer, location, wireid)
		}
	}
 }
