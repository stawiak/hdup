package com.os.actor.service

import akka.actor.{PoisonPill, Props}
import com.os.measurement._
import com.os.actor.write.WriterMasterAware
import com.os.actor._
import util._
import com.os.util._
import com.os.measurement.EnergyMeasurement
import akka.pattern.ask
import com.os.interpolation.NQueue
import akka.util.Timeout
import concurrent.duration._
import concurrent.Future
import com.os.dao.AggregatorState
import akka.pattern.pipe

/**
 * Rollup by customer and location
 *
 * @author Vadim Bobrov
 */
class AggregatorActor(val customer: String, val location: String, var timeWindow : Duration, val timeSource: TimeSource = new TimeSource {}, mockFactory: Option[ActorCache[String]] = None) extends FinalCountDown with WriterMasterAware with TimedActor {

	import context._
	implicit val timeout: Timeout = 10 seconds

	val defaultFactory = CachingActorFactory[String]((String) => actorOf(Props(new InterpolatorActor())))
	val interpolators: ActorCache[String] = if (mockFactory.isEmpty) defaultFactory else mockFactory.get

	var rollups: TimeWindowMap[Long, Double] = new TimeWindowSortedMap[Long, Double]()


	override val lastWill: () => Unit = () => {
		log.debug("saving remaining rollups")
		// save remaining rollups
		for( tv <- rollups)
			writeMaster !  new EnergyMeasurement(customer, location, "", tv._1, tv._2) with Rollup
	}

	override def receive: Receive = {

		// received back from interpolator - add to rollups and save to storage
		case ismt : Interpolated =>
			val m = ismt.asInstanceOf[EnergyMeasurement]
			if (!rollups.contains(m.timestamp))
				rollups += (m.timestamp -> m.value)
			else
				rollups += (m.timestamp -> (m.value + rollups(m.timestamp)))

			writeMaster ! ismt

		// send for interpolation
		case msmt : EnergyMeasurement => interpolators(msmt.wireid) ! msmt

		case Monitor =>
			sender ! Map[String, Long]("rollups" -> rollups.size, "interpolators" -> interpolators.keys.size)

		// flush old rollups
		case Tick => processRollups()

		case SaveState =>
			log.debug("aggregator received SaveState")
			//TODO dump remaining rollups - watch for new messages as depressionMode = false, beware of sortWith not implemented yet in TimeWindowMap
			lastWill()

			collectState pipeTo writeMaster

		case GracefulStop =>
			log.debug("aggregator received GracefulStop")
			//TODO dump remaining rollups - watch for new messages as depressionMode = false, beware of sortWith not implemented yet in TimeWindowMap
			waitAndDie(depressionMode = false)
			children foreach (_ ! PoisonPill)
	}

	private def processRollups() {
		val current = timeSource.now()
		// if any of the rollups are more than 9.5 minutes old
		// save to storage and discard
		val(oldmsmt, newmsmt) = rollups span (current - _._1 > timeWindow.toMillis)

		for( tv <- oldmsmt)
			writeMaster !  new EnergyMeasurement(customer, location, "", tv._1, tv._2) with Rollup

		// discard old values
		rollups = newmsmt
	}

	private def collectState:Future[AggregatorState] = {
		val interpolatorStates = interpolators.keys map (key => {
			for {
				futureKey <- Future { key }
				futureNQueue <- (interpolators(key) ? SaveState).mapTo[NQueue]
			} yield (futureKey, futureNQueue)
		})

		for {
			futureCustomer <- Future { customer }
			futureLocation <- Future { location }
			is <- Future.sequence(interpolatorStates)
		} yield new AggregatorState(futureCustomer, futureLocation, is)
	}

 }
