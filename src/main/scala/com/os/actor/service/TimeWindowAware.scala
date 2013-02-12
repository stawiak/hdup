package com.os.actor.service

import akka.actor.Actor

/**
 * Actor aware of time window
 * @author Vadim Bobrov
*/
trait TimeWindowAware {

	this: Actor =>
	lazy val timeWindow = context.system.actorFor("/user/top/timeWindow")

}
