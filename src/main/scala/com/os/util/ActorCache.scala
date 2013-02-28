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

/**
 * L is a client lookup type
 * K is used for creation of objects
 * K.id is used as map keys
 * @author Vadim Bobrov
 */
trait MappableActorCache[L, K <: {val id: Int} ] {
	def values: Traversable[ActorRef]
	def keys: Traversable[Int]
	def apply(lookUpKey: L)(implicit context: ActorContext) : ActorRef
	def size:Int = keys.size
}

object MappableCachingActorFactory {
	def apply[L, K <: {val id: Int}](mapper: L => K, creator: K => ActorRef)(implicit context: ActorContext): MappableActorCache[L, K] = new MappableCachingActorFactoryImpl[L, K](mapper, creator, context)

	private class MappableCachingActorFactoryImpl[L, K <: {val id: Int}](mapper: L => K, creator: K => ActorRef, val context: ActorContext) extends MappableActorCache[L, K] {
		var created = Map[Int, ActorRef]()

		def values: Traversable[ActorRef] = created.values
		def keys: Traversable[Int] = created.keys

		def apply(lookUpKey: L)(implicit context: ActorContext) : ActorRef = {
			val realKey = mapper(lookUpKey)
			if (!created.contains(realKey.id))
				created += (realKey.id -> creator(realKey))

			created(realKey.id)
		}

	}

}

