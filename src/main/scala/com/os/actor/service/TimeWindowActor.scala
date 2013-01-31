package com.os.actor.service

import akka.actor.{ActorRef, Props}
import com.os.measurement.Measurement
import com.os.Settings
import com.os.actor._

/**
  * @author Vadim Bobrov
  */
class TimeWindowActor(var expiredTimeWindow : Int = Settings.ExpiredTimeWindow) extends FinalCountDown with TimedActor {

	import context._

	override val interval = Settings.TimeWindowProcessInterval

	var measurements = List[Measurement]()

	var aggregatorFactory  : (String, String) => ActorRef = DefaultAggregatorFactory.get

	override def receive: Receive = {

		case msmt : Measurement =>

			//TODO instead of tagging measurement one can also use pattern matching on sender
			//TODO aggregation should be done by .... what???
			measurements ::= msmt

		// send old measurements for aggregation and interpolation
		case Tick => processWindow

		case Monitor =>
			log.info("time window length: {}", measurements.length)
			sender ! Map("length" -> measurements.length)

		case GracefulStop =>
			log.debug("time window received graceful stop")
			// send out remaining measurements
			for (tv <- measurements.sortWith(_ < _))
				aggregatorFactory(tv.customer, tv.location) ! tv

			children foreach ( _ ! GracefulStop)
			waitAndDie()

	}


	/**
	 * send events older than a time window for aggregation and interpolation
	 * and remove them from window
	 */
	private def processWindow() {
		log.debug("processing time window")
		try {
			val current = System.currentTimeMillis()
			// if any of the existing measurements are more than 9.5 minutes old
			// sort by time, interpolate, save to storage and discard
			val oldmsmt = (measurements filter (current - _.timestamp > expiredTimeWindow)).sortWith(_ < _)
			log.debug("old values to send {}", oldmsmt.size)

			for (tv <- oldmsmt)
				aggregatorFactory(tv.customer, tv.location) ! tv

			// discard old values
			measurements = measurements filter (current - _.timestamp <= expiredTimeWindow)
		} catch {
			case e: Exception =>
				log.error(e, e.getMessage)
				//log.debug(measurements.mkString("\n"))
		}

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
