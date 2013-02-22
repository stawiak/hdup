package com.os.util

import akka.actor.{ActorRef, ActorContext}

/**
 * @author Vadim Bobrov
 */
trait ActorCache[T] {
	def getAll: Traversable[ActorRef]
	def apply(t: T)(implicit context: ActorContext) : ActorRef
}

object CachingActorFactory {
	def apply[T](creator: T => ActorRef): ActorCache[T] = new CachingActorFactoryImpl[T](creator)

	private class CachingActorFactoryImpl[T](creator: T => ActorRef) extends ActorCache[T] {
		var created = Map[T, ActorRef]()

		def getAll: Traversable[ActorRef] = created.values

		def apply(t: T)(implicit context: ActorContext) : ActorRef = {
			if (!created.contains(t))
				created += (t -> creator(t))

			created(t)
		}

	}

}
