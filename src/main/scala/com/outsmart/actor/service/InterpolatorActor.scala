package com.outsmart.actor.service

import akka.actor.{ActorLogging, Actor}
import com.outsmart.measurement._
import scala.Some

/**
 * Actor interface to interpolation. Given a measurement send back interpolated values
 *
 * @author Vadim Bobrov
 */
class InterpolatorActor(val boundary: Int = 60000) extends Actor with ActorLogging{

	val tag = Some(new Tag("interpolated"))
	var tv1, tv2, tv3, tv4 : Option[TimedValue] = None

	protected def receive: Receive = {

		case msmt : Measurement => {

			tv1 = tv2; tv2 = tv3; tv3 = tv4; tv4 = Some(new TimedValue(msmt.timestamp, msmt.energy))

			// all 4 points filled? send back interpolated values
			if (tv1 != None && tv2 != None && tv3 != None && tv4 != None)
				for (tv <- Interpolator.bilinear(tv1.get, tv2.get, tv3.get, tv4.get, boundary)) {
					val interpolated = new InterpolatedMeasurement(msmt.customer, msmt.location, msmt.wireid, tv.timestamp, tv.value, msmt.current, msmt.vampire, tag)
					sender ! interpolated
				}

		}

	}



 }
