package com.outsmart.actor.service

import akka.actor.{ActorRef, Props}
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.actor._

/**
  * @author Vadim Bobrov
  */
class TimeWindowActor(var expiredTimeWindow : Int = Settings.ExpiredTimeWindow) extends FinalCountDown with TimedActor {

	import context._

	var measurements = List[Measurement]()

	var aggregatorFactory  : (String, String) => ActorRef = DefaultAggregatorFactory.get

	protected def receive: Receive = {

		case msmt : Measurement =>

			//TODO instead of tagging measurement one can also use pattern matching on sender
			//TODO aggregation should be done by .... what???
			measurements ::= msmt

			processWindow

		// send old measurements for aggregation and interpolation
		case Tick => processWindow

		case GracefulStop =>
			log.debug("time window received graceful stop")
			// send out remaining measurements
			for (tv <- measurements)
				aggregatorFactory(tv.customer, tv.location) ! tv

			children foreach ( _ ! GracefulStop)
			onBlackSpot()

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
				log.debug("creating new aggregator for " + customer + " " + location)
				aggregators += ((customer, location) -> actorOf(Props(new AggregatorActor(customer, location, timeWindow = expiredTimeWindow))))
			}

			aggregators(customer, location)
		}
	}
 }
