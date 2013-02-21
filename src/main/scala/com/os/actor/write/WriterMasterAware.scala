package com.os.actor.write

import akka.actor.Actor

/**
 * Actor aware of write master
 * @author Vadim Bobrov
*/
trait WriterMasterAware {

	this: Actor =>
	lazy val writeMaster = context.system.actorFor("/user/top/writeMaster")

}
