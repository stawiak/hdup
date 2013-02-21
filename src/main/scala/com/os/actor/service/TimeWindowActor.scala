package com.os.actor.service

import akka.actor.{ActorContext, ActorRef}
import com.os.measurement.{EnergyMeasurement, Measurement}
import com.os.actor._
import util._
import write.WriterMasterAware
import concurrent.duration.Duration
import com.os.util.{CachingActorFactory, TimeWindowListBuffer, TimeSource, TimeWindow}


/**
  * @author Vadim Bobrov
  */
class TimeWindowActor(var expiredTimeWindow : Duration, val timeSource: TimeSource = new TimeSource {}, mockFactory: Option[(ActorContext, (String, String)) => ActorRef] = None) extends FinalCountDown with WriterMasterAware with TimedActor with SettingsUse {

	import context._

	override val interval = settings.TimeWindowProcessInterval
  	val interpolation = settings.Interpolation

	var measurements:TimeWindow[Measurement] = new TimeWindowListBuffer[Measurement]()

	val defaultFactory = new CachingActorFactory[(String, String)]((customerLocation: (String, String)) => new AggregatorActor(customerLocation._1, customerLocation._2, timeWindow = expiredTimeWindow))
	val aggregatorFactory: (ActorContext, (String, String)) => ActorRef = if (mockFactory.isEmpty) defaultFactory.get else mockFactory.get

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
			log.info("time window length: {}", measurements.size)
			sender ! Map("length" -> measurements.size)

		case GracefulStop =>
			log.debug("time window received graceful stop")
			// send out remaining measurements
			for (tv <- measurements.sortWith(_ < _))
				aggregatorFactory(context, (tv.customer, tv.location)) ! tv

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
			//log.debug("old " + oldmsmt.size)
			//log.debug("new " + newmsmt.size)

			for (tv <- oldmsmt.sortWith(_ < _))
				aggregatorFactory(context, (tv.customer, tv.location)) ! tv

			// discard old values
			measurements = newmsmt
		} catch {
			case e: Exception =>
				log.error(e, e.getMessage)
				//log.debug(measurements.mkString("\n"))
		}

	}

 }
