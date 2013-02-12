package com.os.actor.read

import akka.actor.Actor

/**
 * Actor aware of read master
 * @author Vadim Bobrov
*/
trait MQLHandlerAware {

	this: Actor =>
	lazy val mqlHandler = context.system.actorFor("/user/top/mqlHandler")

}
