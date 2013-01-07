package com.outsmart.actor.service

import akka.actor.{Props, ActorLogging, Actor}
import com.outsmart.measurement.{TimedValue, Interpolator, Measurement}
import com.outsmart.actor.write.WriteMasterActor
import com.outsmart.Settings

/**
 * Actor interface to interpolation. Given a measurement send back interpolated values
 *
 * @author Vadim Bobrov
 */
class InterpolatorActor(val boundary: Int = 60000) extends Actor with ActorLogging{

	import context._
	var tv1, tv2, tv3, tv4 : Option[TimedValue] = None

	protected def receive: Receive = {

		case msmt : Measurement => {

			tv1 = tv2; tv2 = tv3; tv3 = tv4; tv4 = msmt

			// all 4 points filled? send back interpolated values
			if (tv1 != None && tv2 != None && tv3 != None && tv4 != None)
				for (tv <- Interpolator.bilinear(tv1.get, tv2.get, tv3.get, tv4.get, boundary))
					sender ! tv

		}

	}



 }
