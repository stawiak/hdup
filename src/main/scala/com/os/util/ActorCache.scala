package com.os.util

import akka.actor.{Props, Actor, ActorRef, ActorContext}

/**
 * @author Vadim Bobrov
 */
trait ActorCache[T] {
	def getAll: Traversable[ActorRef]
	def apply(actorContext: ActorContext, t: T) : ActorRef
}

object CachingActorFactory {
	def apply[T](creator: T => Actor): ActorCache[T] = new CachingActorFactoryImpl[T](creator)

	private class CachingActorFactoryImpl[T](creator: T => Actor) extends ActorCache[T] {
		var created = Map[T, ActorRef]()

		def getAll: Traversable[ActorRef] = created.values

		def apply(actorContext: ActorContext, t: T) : ActorRef = {
			if (!created.contains(t))
				created += (t -> actorContext.actorOf(Props(creator(t))))

			created(t)
		}

	}

}
