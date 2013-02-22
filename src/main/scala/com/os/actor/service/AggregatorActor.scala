package com.os.actor.service

import akka.actor.{PoisonPill, Props, ActorRef}
import com.os.measurement._
import com.os.actor.write.WriterMasterAware
import com.os.actor._
import concurrent.duration.Duration
import util._
import com.os.util.{TimeWindowSortedMap, TimeWindowHashMap, TimeWindowMap, TimeSource}
import com.os.measurement.EnergyMeasurement

/**
 * Rollup by customer and location
 *
 * @author Vadim Bobrov
 */
class AggregatorActor(val customer: String, val location: String, var timeWindow : Duration, val timeSource: TimeSource = new TimeSource {}) extends FinalCountDown with WriterMasterAware with TimedActor with SettingsUse {

	import context._
	var interpolatorFactory  : String => ActorRef = DefaultInterpolatorFactory.get
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
		case msmt : EnergyMeasurement => interpolatorFactory(msmt.wireid) ! msmt

		// flush old rollups
		case Tick => processRollups()

		case GracefulStop =>
			log.debug("aggregator received graceful stop")
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

	object DefaultInterpolatorFactory {
		var interpolators = Map[String, ActorRef]()

		def get(wireid : String) : ActorRef = {
			if (!interpolators.contains(wireid)) {
				log.debug("creating new interpolator for " + wireid)
				interpolators += (wireid -> actorOf(Props(new InterpolatorActor())))
			}

			interpolators(wireid)
		}
	}


 }
