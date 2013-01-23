package com.os.actor.service

import akka.actor.{PoisonPill, Props, ActorRef}
import com.os.measurement._
import com.os.actor.write.WriterMasterAwareActor
import com.os.actor._
import com.os.Settings

/**
 * Rollup by customer and location
 *
 * @author Vadim Bobrov
 */
class AggregatorActor(val customer: String, val location: String, var timeWindow : Int = Settings.ExpiredTimeWindow) extends FinalCountDown with WriterMasterAwareActor with TimedActor {

	import context._
	var interpolatorFactory  : String => ActorRef = DefaultInterpolatorFactory.get
	var rollups = Map[Long, Double]()


	override val lastWill: () => Unit = () => {
		// save remaining rollups
		for( tv <- rollups)
			writeMaster !  new Measurement(customer, location, "", tv._1, tv._2, 0, 0) with Rollup
	}

	override def receive: Receive = {

		// received back from interpolator - add to rollups and save to storage
		case ismt : Interpolated =>
			val m = ismt.asInstanceOf[Measurement]
			if (!rollups.contains(m.timestamp))
				rollups += (m.timestamp -> m.energy)
			else
				rollups += (m.timestamp -> (m.energy + rollups(m.timestamp)))

			writeMaster ! ismt

		// send for interpolation
		case msmt : Measurement => interpolatorFactory(msmt.wireid) ! msmt

		// flush old rollups
		case Tick => processRollups()

		case GracefulStop =>
			log.debug("aggregator received graceful stop")
			waitAndDie(depressionMode = false)
			children foreach (_ ! PoisonPill)
	}

	private def processRollups() {
		val current = System.currentTimeMillis()
		// if any of the rollups are more than 9.5 minutes old
		// save to storage and discard
		val oldmsmt = rollups filter (current - _._1 > timeWindow)


		for( tv <- oldmsmt)
			writeMaster !  new Measurement(customer, location, "", tv._1, tv._2, 0, 0) with Rollup

		// discard old values
		rollups = rollups filter (current - _._1 <= timeWindow)
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
