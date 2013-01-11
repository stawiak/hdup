package com.outsmart.actor.service

import com.outsmart.measurement._
import scala.Some
import com.outsmart.actor.write.WriterMasterAwareActor

/**
 * Actor interface to interpolation. Given a measurement send back interpolated values
 *
 * @author Vadim Bobrov
 */
class InterpolatorActor(val boundary: Int = 60000) extends WriterMasterAwareActor {

	var tv1, tv2, tv3, tv4 : Option[TimedValue] = None

	protected def receive: Receive = {

		case msmt : Measurement => {

			tv1 = tv2; tv2 = tv3; tv3 = tv4; tv4 = Some(new TimedValue(msmt.timestamp, msmt.energy))

			// all 4 points filled? send back interpolated values
			if (tv1 != None && tv2 != None && tv3 != None && tv4 != None)
				for (tv <- Interpolator.bilinear(tv1.get, tv2.get, tv3.get, tv4.get, boundary)) {
					val interpolated = new Measurement(msmt.customer, msmt.location, msmt.wireid, tv.timestamp, tv.value, msmt.current, msmt.vampire) with Interpolated
					writeMaster ! interpolated
				}

		}

	}



 }
