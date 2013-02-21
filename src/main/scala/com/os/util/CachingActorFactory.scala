package com.os.util

import akka.actor.{Actor, Props, ActorContext, ActorRef}

/**
 * @author Vadim Bobrov
 */
class CachingActorFactory[T](creator: T => Actor) {
	var created = Map[T, ActorRef]()

	def get(actorContext: ActorContext, t: T) : ActorRef = {
		if (!created.contains(t))
			created += (t -> actorContext.actorOf(Props(creator(t))))

		created(t)
	}

}
