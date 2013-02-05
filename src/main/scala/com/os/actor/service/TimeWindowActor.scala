package com.os.actor.service

import akka.actor.{ActorRef, Props}
import com.os.measurement.Measurement
import com.os.actor._
import util._
import write.WriterMasterAware
import concurrent.duration.Duration
import com.os.util.{TimeWindowSortedSetBuffer, TimeSource, TimeWindow}


/**
  * @author Vadim Bobrov
  */
class TimeWindowActor(var expiredTimeWindow : Duration, val timeSource: TimeSource = new TimeSource {}) extends FinalCountDown with WriterMasterAware with TimedActor with SettingsUse {

	import context._

	override val interval = settings.TimeWindowProcessInterval

	var measurements:TimeWindow[Measurement] = new TimeWindowSortedSetBuffer[Measurement]()
	var aggregatorFactory  : (String, String) => ActorRef = DefaultAggregatorFactory.get

	override def receive: Receive = {

		case msmt : Measurement =>
			writeMaster ! msmt

			// if less than 9.5 minutes old - add to time window
			if (timeSource.now - msmt.timestamp < expiredTimeWindow.toMillis)
				measurements += msmt


		// send old measurements for aggregation and interpolation
		case Tick => processWindow

		case Monitor =>
			log.info("time window length: {}", measurements.size)
			sender ! Map("length" -> measurements.size)

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
		try {
			val current = timeSource.now()
			// if any of the existing measurements are more than 9.5 minutes old
			// sort by time, interpolate, save to storage and discard
			val(oldmsmt, newmsmt) = measurements span (current - _.timestamp > expiredTimeWindow.toMillis)

			for (tv <- oldmsmt.sortWith(_ < _))
				aggregatorFactory(tv.customer, tv.location) ! tv

			// discard old values
			measurements = newmsmt
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
