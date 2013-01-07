package com.outsmart.actor.service

import akka.actor.{Props, ActorLogging, Actor}
import com.outsmart.measurement.{TimedValue, Interpolator, Measurement}
import com.outsmart.actor.write.WriteMasterActor
import com.outsmart.Settings

/**
  * @author Vadim Bobrov
  */
class TimeWindowActor extends Actor with ActorLogging{

	import context._

	var measurements = List[Measurement]()
	val writeMaster = actorOf(Props(new WriteMasterActor()), name = "master")
	val interpolator = actorOf(Props(new InterpolatorActor()), name = "interpolator")


	protected def receive: Receive = {

		case msmt : Measurement => {

			if (!msmt.interpolated) {
				val current = System.currentTimeMillis()
				// if any of the existing measurements are more than 9.5 minutes old
				// sort by time, interpolate, save to storage and discard
				for (tv <- (measurements filter (current - _.timestamp < Settings.ExpiredTimeWindow) sorted))
					interpolator ! tv
			} else
				// save interpolated value
				writeMaster ! msmt

		}

	}



 }
