package com.os

import akka.actor.{ActorContext, ActorRef}
import util.ActorCache

/**
 * @author Vadim Bobrov
 */
class TestActorCacheAdapter[T](factory: (ActorContext, T) => ActorRef) extends ActorCache[T] {
		def values: Traversable[ActorRef] = Nil
		def keys: Traversable[T] = Nil
		def apply(t: T)(implicit context: ActorContext): ActorRef = factory(context, t)
}
