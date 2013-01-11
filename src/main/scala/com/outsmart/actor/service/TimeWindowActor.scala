package com.outsmart.actor.service

import akka.actor.{ActorRef, Props}
import akka.util.duration._
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.{WriterMasterAwareActor, GracefulStop}
import com.outsmart.Settings
import com.outsmart.actor.DoctorGoebbels
import akka.util.Duration

/**
  * @author Vadim Bobrov
  */
case object Tick
class TimeWindowActor(var expiredTimeWindow : Int = Settings.ExpiredTimeWindow) extends WriterMasterAwareActor with DoctorGoebbels {

	import context._

	var measurements = List[Measurement]()
	var isStopped = false

	var aggregatorFactory  : (String, String) => ActorRef = DefaultAggregatorFactory.get

	override def preStart() {
		super.preStart()

		system.scheduler.schedule(Duration.Zero, 1000 milliseconds, self, Tick)
	}



	protected def receive: Receive = {

		case msmt : Measurement =>

			//TODO instead of tagging measurement one can also use pattern matching on sender
			//TODO aggregation should be done by .... what???
			measurements ::= msmt

			processWindow

		// clean window
		case Tick =>
			log.debug("tick received " + measurements.length + " left")

			processWindow
			if (isStopped && measurements.isEmpty) {
				writeMaster ! GracefulStop
				onBlackSpot()
			}


		case GracefulStop =>
			log.debug("time window received graceful stop")
			isStopped = true

	}


	/**
	 * send events older than a time window for aggregation and interpolation
	 * and remove them from window
	 */
	private def processWindow() {
		val current = System.currentTimeMillis()
		// if any of the existing measurements are more than 9.5 minutes old
		// sort by time, interpolate, save to storage and discard
		val oldmsmt = (measurements filter (current - _.timestamp > expiredTimeWindow)).sortWith(_ < _)

		for (tv <- oldmsmt) {
			//log.info("sending to interpolation")
			aggregatorFactory(tv.customer, tv.location) ! tv
		}
		// discard old values
		measurements = measurements filter (current - _.timestamp <= expiredTimeWindow)

	}

	object DefaultAggregatorFactory {
		var aggregators = Map[(String, String), ActorRef]()

		def get(customer : String, location : String) : ActorRef = {
			if (!aggregators.contains(customer, location)) {
				log.info("creating new aggregator for " + customer + " " + location)
				aggregators += ((customer, location) -> actorOf(Props(new AggregatorActor())))
			}

			aggregators(customer, location)
		}
	}
 }
