package com.os.actor.service

import akka.actor.{ActorLogging, Props}
import com.os.measurement._
import com.os.actor.write.WriterMasterAware
import com.os.actor._
import read.ReadMasterAware
import util._
import com.os.util._
import com.os.measurement.EnergyMeasurement
import akka.pattern.ask
import com.os.interpolation.NQueue
import akka.util.Timeout
import concurrent.duration._
import concurrent.{Await, Future}
import com.os.dao.AggregatorState
import javax.management.ObjectName
import java.util.UUID

/**
 * Rollup by customer and location
 *
 * @author Vadim Bobrov
 */
trait AggregatorActorMBean {
	def getInterpolatorInfo: Array[String]
}
class AggregatorActor(
						 customer: String,
						 location: String,
						 timeWindow : Duration,
						 timeSource: TimeSource = new TimeSource {},
						 aggregatorState: Option[AggregatorState] = None,
						 mockFactory: Option[ActorCache[String]] = None
					)

	extends JMXNotifier with JMXActorBean with ActorLogging with WriterMasterAware with ReadMasterAware with TimedActor with AggregatorActorMBean {


	import context._
	override val jmxName = new ObjectName("com.os.chaos:type=TimeWindow,TimeWindow=aggregators,name=\"" + customer + "@" + location + "\"")
	implicit val timeout: Timeout = 10 seconds

	val queueMap = if (!aggregatorState.isEmpty) aggregatorState.get.interpolatorStates.toMap else Map.empty[String, NQueue]
	val defaultFactory = CachingActorFactory[String]((key: String) =>
			actorOf(Props(new InterpolatorActor(queueMap.get(key))))
	)

	val interpolators: ActorCache[String] = mockFactory.getOrElse(defaultFactory)

	var rollups: TimeWindowMap[Long, Double] = new TimeWindowSortedMap[Long, Double]()
	val disableGroup = GroupMessage(() => Disable())
	var reportDisabledId: UUID = _

	def getInterpolatorInfo:Array[String] = interpolators.keys.toArray

	override def receive: Receive = {

		// received back from interpolator - add to rollups and save to storage
		case ismt: Interpolated => onInterpolated(ismt)

		// send for interpolation
		case msmt : EnergyMeasurement => interpolators(msmt.wireid) ! msmt

		case Monitor =>
			sender ! Map[String, Long]("rollups" -> rollups.size, "interpolators" -> interpolators.keys.size)

		// flush old rollups
		case Tick => processRollups()

		case Disable(id) =>
			reportDisabledId = id
			// will not receive Tick no more but some might still be in the mailbox
			// they will not be processed as we become collecting/deaf at the end of Disable
			cancelSchedule()

			// we need to continue processing interpolations until all kids report done
			// expect Done and Interpolation only
			become(collecting)

			// ask children to send Done when done
			children foreach (_ ! disableGroup.newMessage())

	}

	def collecting: Receive = {
		case ismt: Interpolated => onInterpolated(ismt)

		case Disabled(id) =>
			disableGroup.receive(id)

			// then become deaf and expect SaveState only
			if (disableGroup.isDone) {
				// only flush to write master when all interpolations have been received
				flush()
				become(disabled)
				parent ! Disabled(reportDisabledId)
			}
	}

	def disabled: Receive = {
		case SaveState =>
			log.debug("aggregator received SaveState")
			// this must be fully synchronous or else write master could be killed prematurely
			sender ! Await.result(collectState, 10 seconds)
	}


	private def onInterpolated(ismt: Interpolated) {
		val m = ismt.asInstanceOf[EnergyMeasurement]
		if (!rollups.contains(m.timestamp))
			rollups += (m.timestamp -> m.value)
		else
			rollups += (m.timestamp -> (m.value + rollups(m.timestamp)))

		writeMaster ! ismt
	}

	private def processRollups() {

		val current = timeSource.now()
		// if any of the rollups are more than 9.5 minutes old
		// save to storage and discard
		val(oldmsmt, newmsmt) = rollups span (current - _._1 > timeWindow.toMillis)

		for(tv <- oldmsmt)
			writeMaster !  new EnergyMeasurement(customer, location, "", tv._1, tv._2) with Rollup

		// discard old values
		rollups = newmsmt
	}

	private def flush() {
		//log.info("saving remaining rollups")
		// save remaining rollups
		for( tv <- rollups)
			writeMaster !  new EnergyMeasurement(customer, location, "", tv._1, tv._2) with Rollup
		rollups = new TimeWindowSortedMap[Long, Double]()
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
