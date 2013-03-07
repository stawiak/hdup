package com.os.actor.util

import akka.actor.{Cancellable, Actor}
import concurrent.duration._


/**
 * Actor that sends itself time ticks to react and do some processing
 * @author Vadim Bobrov
*/
case object Tick
trait TimedActor extends Actor {

	val interval: FiniteDuration = 5 seconds

	import context.dispatcher

	var schedule: Cancellable = _

	abstract override def preStart() {
		schedule = context.system.scheduler.schedule(Duration.Zero, interval, self, Tick)
		super.preStart()
	}

	abstract override def postStop() {
		schedule.cancel()
		super.postStop()
	}

	def updateInterval(newInterval: FiniteDuration) {
		schedule.cancel()
		schedule = context.system.scheduler.schedule(Duration.Zero, newInterval, self, Tick)
	}
}
