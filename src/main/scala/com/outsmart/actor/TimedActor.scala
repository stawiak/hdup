package com.outsmart.actor

import akka.actor.{ActorLogging, Actor}
import akka.util.Duration
import akka.util.duration._

/**
 * Actor that sends itself time ticks to react and do some processing
 * @author Vadim Bobrov
*/
case object Tick
trait TimedActor extends Actor with ActorLogging {

	override def preStart() {
		super.preStart()

		context.system.scheduler.schedule(Duration.Zero, 1000 milliseconds, self, Tick)
	}

}
