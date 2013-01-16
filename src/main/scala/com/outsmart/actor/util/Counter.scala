package com.outsmart.actor.util

import akka.agent.Agent
import akka.actor.ActorSystem

/**
 * Transactional counter using akka agents
 * @author Vadim Bobrov
 */
class Counter(implicit val actorSystem: ActorSystem) extends Agent[Int](initialValue = 0, system = actorSystem) {

	def ++() {
		this send (_ + 1)
	}

}
