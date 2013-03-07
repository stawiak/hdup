package com.os.actor.service

import akka.actor.Props
import com.os.measurement.{EnergyMeasurement, Measurement}
import com.os.actor._
import read.{LoadState, ReadMasterAware}
import util._
import write.WriterMasterAware
import com.os.util._
import com.os.Settings
import com.os.dao.{TimeWindowState, AggregatorState}
import concurrent.{Await, Future}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import javax.management.ObjectName


/**
  * @author Vadim Bobrov
  */
trait TimeWindowActorMBean
class TimeWindowActor(var expiredTimeWindow : Duration, val timeSource: TimeSource = new TimeSource {}, mockFactory: Option[ActorCache[(String, String)]] = None) extends JMXActorBean with FinalCountDown with WriterMasterAware with ReadMasterAware with TimedActor with TimeWindowActorMBean {

	import context._

	override val jmxName = new ObjectName("com.os.chaos:type=TimeWindow,name=timeWindow")

	type AggregatorStates = Map[(String, String), AggregatorState]
	implicit val timeout: Timeout = 10 seconds
	override val interval = Settings().TimeWindowProcessInterval

	var measurements:TimeWindow[Measurement] = new TimeWindowSortedSetBuffer[Measurement]()

	val defaultFactory = CachingActorFactory[(String, String)]((customerLocation: (String, String)) => actorOf(Props(new AggregatorActor(customerLocation._1, customerLocation._2, timeWindow = expiredTimeWindow))))
	var aggregators: ActorCache[(String, String)] = mockFactory.getOrElse(defaultFactory)

	override def receive: Receive = {

		case msmt : EnergyMeasurement =>
			measurements += msmt

		// send old measurements for aggregation and interpolation
		case Tick => processWindow

		case Monitor =>
			sender ! Map[String, Any]("length" -> measurements.size, "aggregators" -> aggregators.keys.size, "aggregatorNames" -> aggregators.keys)
			aggregators.values foreach (_ forward Monitor)

		case SaveState =>
			log.debug("time window received SaveState")
			flush()

			val timeWindowState =
				for ( aggState <- Future.traverse(children)(child => (child ? SaveState).mapTo[AggregatorState]) )
				yield new TimeWindowState(aggState)

			// this must be fully synchronous or else write master could be killed prematurely
			writeMaster ! Await.result(timeWindowState, 10 seconds)

		case LoadState =>
			log.debug("time window received LoadState")
			readMaster ! LoadState(self.path)

		case states: AggregatorStates =>
			log.debug("received AggregatorStates with {} elements", states.size)
			states.values foreach  { v =>
				log.debug("\t {}", v.customer)
				log.debug("\t {} {}", v.location, v.interpolatorStates.size)
				v.interpolatorStates foreach (sv =>
					log.debug("\t\t{}\t{}", sv._1, sv._2)
				)
			}
			aggregators = CachingActorFactory[(String, String)]((customerLocation: (String, String)) => actorOf(Props(
				new AggregatorActor(customerLocation._1, customerLocation._2, timeWindow = expiredTimeWindow, aggregatorState = states.get(customerLocation))
			)))

		case GracefulStop =>
			log.debug("time window received GracefulStop")

			flush()
			waitAndDie()
			children foreach ( _ ! GracefulStop)


	}

	/**
	 * send out remaining measurements
	 */
	private def flush() {
		for (tv <- measurements)
			aggregators((tv.customer, tv.location)) ! tv
		// this is important!!! or time window tick will kick in on the same msmts
		measurements = new TimeWindowSortedSetBuffer[Measurement]()
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

			for (tv <- oldmsmt)
				aggregators((tv.customer, tv.location)) ! tv

			// discard old values
			measurements = newmsmt
		} catch {
			case e: Exception =>
				log.error(e, e.getMessage)
		}

	}

}

