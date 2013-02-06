package com.os.actor.service

import com.os.measurement._
import scala.Some
import akka.actor.{ActorLogging, Actor}

/**
 * Actor interface to interpolation. Given a measurement send back interpolated values
 *
 * @author Vadim Bobrov
 */
class InterpolatorActor(val boundary: Int = 60000) extends Actor with ActorLogging {

	var tv1, tv2, tv3, tv4 : Option[TimedValue] = None

	override def receive: Receive = {

		case msmt : EnergyMeasurement => {

			tv1 = tv2; tv2 = tv3; tv3 = tv4; tv4 = Some(new TimedValue(msmt.timestamp, msmt.value))

			// all 4 points filled? send back interpolated values
			if (tv1 != None && tv2 != None && tv3 != None && tv4 != None)
				for (tv <- Interpolator.bilinear(tv1.get, tv2.get, tv3.get, tv4.get, boundary)) {
					val interpolated = new EnergyMeasurement(msmt.customer, msmt.location, msmt.wireid, tv.timestamp, tv.value) with Interpolated
					sender ! interpolated
				}

		}

	}



 }
