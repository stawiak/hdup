package com.os.actor.read

import akka.actor.Actor

/**
 * Actor aware of read master
 * @author Vadim Bobrov
*/
trait ReadMasterAware {

	this: Actor =>
	lazy val readMaster = context.system.actorFor("/user/top/readMaster")

}
