package com.outsmart.actor.service

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.util.duration._
import com.outsmart.measurement.{InterpolatedMeasurement, TimedValue, Interpolator, Measurement}
import com.outsmart.actor.write.WriteMasterActor
import com.outsmart.Settings

/**
  * @author Vadim Bobrov
  */
case object Tick
class TimeWindowActor extends Actor with ActorLogging{

	import context._

	var measurements = List[Measurement]()
	val writeMaster = actorOf(Props(new WriteMasterActor()), name = "master")

	var interpolators = Map[(String, String, String), ActorRef]()


	override def preStart() {
		super.preStart()
		system.scheduler.schedule(0 milliseconds, 1000 milliseconds, self, Tick)
	}

	protected def receive: Receive = {

		// save interpolated value
		case imsmt : InterpolatedMeasurement => writeMaster ! imsmt

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
		for (tv <- (measurements filter (current - _.timestamp > Settings.ExpiredTimeWindow) sorted)) {

			if (!interpolators.contains(tv.customer, tv.location, tv.wireid))
				interpolators += ((tv.customer, tv.location, tv.wireid) -> actorOf(Props(new InterpolatorActor()), name = "interpolator"))

			interpolators(tv.customer, tv.location, tv.wireid) ! tv
		}

		// discard old values
		measurements = measurements filter (current - _.timestamp <= Settings.ExpiredTimeWindow)

	}

 }
