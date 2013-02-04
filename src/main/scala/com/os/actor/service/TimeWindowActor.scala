package com.os.actor.service

import akka.actor.{ActorRef, Props}
import com.os.measurement.Measurement
import com.os.Settings
import com.os.actor._
import util.{Tick, TimedActor, GracefulStop, FinalCountDown}
import write.WriterMasterAware
import concurrent.duration.Duration
import com.os.util.{TimeSource, TimeWindowListBuffer, TimeWindow}


/**
  * @author Vadim Bobrov
  */
class TimeWindowActor(var expiredTimeWindow : Duration = Settings.ExpiredTimeWindow, val timeSource: TimeSource = new TimeSource {}) extends FinalCountDown with WriterMasterAware with TimedActor {

	import context._

	override val interval = Settings.TimeWindowProcessInterval

	var measurements:TimeWindow[Measurement] = new TimeWindowListBuffer[Measurement]()
	var aggregatorFactory  : (String, String) => ActorRef = DefaultAggregatorFactory.get

	override def receive: Receive = {

		case msmt : Measurement =>
			writeMaster ! msmt
			//TODO instead of tagging measurement one can also use pattern matching on sender
			//TODO aggregation should be done by .... what???
			// if less than 9.5 minutes old - add to time window
			if (timeSource.now - msmt.timestamp < Settings.ExpiredTimeWindow.toMillis)
				measurements += msmt


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
