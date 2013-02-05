package com.os.actor.util

import akka.actor.{Cancellable, Actor}
import concurrent.duration._


/**
 * Actor that sends itself time ticks to react and do some processing
 * @author Vadim Bobrov
*/
case object Tick
trait TimedActor {
	this: Actor =>

	val interval: FiniteDuration = 5 seconds

	import context.dispatcher

	var schedule: Cancellable = _

	override def preStart() {
		schedule = context.system.scheduler.schedule(Duration.Zero, interval, self, Tick)
	}

	override def postStop() {
		schedule.cancel()
	}
}