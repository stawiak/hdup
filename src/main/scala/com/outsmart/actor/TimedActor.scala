package com.outsmart.actor

import akka.actor.{Cancellable, ActorLogging, Actor}
import akka.util.Duration
import akka.util.duration._

/**
 * Actor that sends itself time ticks to react and do some processing
 * @author Vadim Bobrov
*/
case object Tick
trait TimedActor extends Actor with ActorLogging {

	var schedule: Cancellable = _

	override def preStart() {
		schedule = context.system.scheduler.schedule(Duration.Zero, 1000 milliseconds, self, Tick)
	}

	override def postStop() {
		schedule.cancel()
	}
}
