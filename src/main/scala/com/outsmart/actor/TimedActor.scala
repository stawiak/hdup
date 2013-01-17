package com.outsmart.actor

import akka.actor.{Cancellable, ActorLogging, Actor}
import concurrent.duration._


/**
 * Actor that sends itself time ticks to react and do some processing
 * @author Vadim Bobrov
*/
case object Tick
trait TimedActor extends Actor with ActorLogging {
	import context.dispatcher

	var schedule: Cancellable = _

	override def preStart() {
		schedule = context.system.scheduler.schedule(Duration.Zero, 1 second, self, Tick)
	}

	override def postStop() {
		schedule.cancel()
	}
}
