package com.os.actor.service

import com.os.measurement._
import akka.actor.{ActorLogging, Actor}
import com.os.actor.util.SaveState
import com.os.interpolation.{NQueueImpl, NQueue}

/**
 * Actor interface to interpolation. Given a measurement send back interpolated values
 *
 * @author Vadim Bobrov
 */
class InterpolatorActor(val boundary: Int = 60000) extends Actor with ActorLogging {

	val queue:NQueue = new NQueueImpl

	override def receive: Receive = {

		case msmt : EnergyMeasurement =>

			queue offer new TimedValue(msmt.timestamp, msmt.value)

			// all 4 points filled? send back interpolated values
			if (queue.isFull)
				for (tv <- Interpolator.bilinear(queue.get(0), queue.get(1), queue.get(2), queue.get(3), boundary)) {
					val interpolated = new EnergyMeasurement(msmt.customer, msmt.location, msmt.wireid, tv.timestamp, tv.value) with Interpolated
					sender ! interpolated
				}

		case SaveState =>
			sender ! queue

	}



 }
