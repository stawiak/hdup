package com.outsmart.actor.service

import akka.actor.{Props, ActorLogging, Actor}
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.WriteMasterActor
import com.outsmart.Settings

/**
  * @author Vadim Bobrov
  */
class IncomingHandlerActor extends Actor with ActorLogging{

	import context._

	val writeMaster = actorOf(Props(new WriteMasterActor()), name = "writeMaster")
	val timeWindowManager = actorOf(Props(new TimeWindowActor()), name = "timeWindow")

	protected def receive: Receive = {

		case msmt : Measurement => {
			writeMaster ! msmt

			// if less than 9.5 minutes old - send to time window manager
			if (System.currentTimeMillis() - msmt.timestamp < Settings.ExpiredTimeWindow)
				timeWindowManager ! msmt
		}

	}

 }
