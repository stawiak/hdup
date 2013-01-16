package com.outsmart.actor.util

import akka.agent.Agent


/**
 * @author Vadim Bobrov
 */
object Stats {

	var sentWriteMaster : Counter = _
	var receivedWriteWorker : Counter = _

}
