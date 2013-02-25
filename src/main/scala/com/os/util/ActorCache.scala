package com.os.util

import akka.actor.{ActorRef, ActorContext}

/**
 * @author Vadim Bobrov
 */
trait ActorCache[T] {
	def values: Traversable[ActorRef]
	def keys: Traversable[T]
	def apply(t: T)(implicit context: ActorContext) : ActorRef
	def size:Int = keys.size
}

object CachingActorFactory {
	def apply[T](creator: T => ActorRef)(implicit context: ActorContext): ActorCache[T] = new CachingActorFactoryImpl[T](creator, context)

	private class CachingActorFactoryImpl[T](creator: T => ActorRef, val context: ActorContext) extends ActorCache[T] {
		var created = Map[T, ActorRef]()

		def values: Traversable[ActorRef] = created.values
		def keys: Traversable[T] = created.keys

		def apply(t: T)(implicit context: ActorContext) : ActorRef = {
			if (!created.contains(t))
				created += (t -> creator(t))

			created(t)
		}

	}

}
