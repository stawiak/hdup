package com.os.actor.service

import akka.actor.{Props, ActorContext, ActorRef}
import com.os.measurement.{EnergyMeasurement, Measurement}
import com.os.actor._
import util._
import write.WriterMasterAware
import concurrent.duration.Duration
import com.os.util._


/**
  * @author Vadim Bobrov
  */
class TimeWindowActor(var expiredTimeWindow : Duration, val timeSource: TimeSource = new TimeSource {}, mockFactory: Option[ActorCache[(String, String)]] = None) extends FinalCountDown with WriterMasterAware with TimedActor with SettingsUse {

	import context._

	override val interval = settings.TimeWindowProcessInterval
  	val interpolation = settings.Interpolation

	var measurements:TimeWindow[Measurement] = new TimeWindowSortedSetBuffer[Measurement]()

	val defaultFactory = CachingActorFactory[(String, String)]((customerLocation: (String, String)) => actorOf(Props(new AggregatorActor(customerLocation._1, customerLocation._2, timeWindow = expiredTimeWindow))))
	val aggregators: ActorCache[(String, String)] = if (mockFactory.isEmpty) defaultFactory else mockFactory.get

	override def receive: Receive = {

		case msmt : EnergyMeasurement =>
			writeMaster ! msmt

			// if less than 9.5 minutes old - add to time window
			if (interpolation && timeSource.now - msmt.timestamp < expiredTimeWindow.toMillis)
				measurements += msmt

    	case msmt : Measurement =>
      		writeMaster ! msmt

		// send old measurements for aggregation and interpolation
		case Tick => processWindow

		case Monitor =>
			sender ! Map[String, Any]("length" -> measurements.size, "aggregators" -> aggregators.keys.size, "aggregatorNames" -> aggregators.keys)
			aggregators.values foreach (_ forward Monitor)

		case GracefulStop =>
			log.debug("time window received graceful stop")
			// send out remaining measurements
			for (tv <- measurements.sortWith(_ < _))
				aggregators((tv.customer, tv.location)) ! tv

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
			val(oldmsmt, newmsmt) = measurements span (_.timestamp < (current - expiredTimeWindow.toMillis))

			for (tv <- oldmsmt.sortWith(_ < _))
				aggregators((tv.customer, tv.location)) ! tv

			// discard old values
			measurements = newmsmt
		} catch {
			case e: Exception =>
				log.error(e, e.getMessage)
		}

	}

 }
