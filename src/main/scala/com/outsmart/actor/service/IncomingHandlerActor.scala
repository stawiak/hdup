package com.outsmart.actor.service

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.WriteMasterActor
import com.outsmart.Settings


/**
  * @author Vadim Bobrov
  */
class IncomingHandlerActor(var writeMaster : ActorRef, var timeWindowManager : ActorRef) extends Actor with ActorLogging{

	import context._

	override def preStart() {
		// initialize slaves to defaults
		if (writeMaster == null)
			writeMaster = actorOf(Props[WriteMasterActor], name = "writeMaster")
		if (timeWindowManager == null)
			timeWindowManager = actorOf(Props[TimeWindowActor], name = "timeWindow")

		super.preStart()
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			writeMaster ! msmt

			// if less than 9.5 minutes old - send to time window manager
			if (System.currentTimeMillis() - msmt.timestamp < Settings.ExpiredTimeWindow)
				timeWindowManager ! msmt
		}

	}

 }
