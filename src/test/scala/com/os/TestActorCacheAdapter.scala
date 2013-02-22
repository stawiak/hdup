package com.os

import akka.actor.{ActorContext, ActorRef}
import util.ActorCache

/**
 * @author Vadim Bobrov
 */
class TestActorCacheAdapter[T](factory: (ActorContext, T) => ActorRef) extends ActorCache[T] {
		def getAll: Traversable[ActorRef] = Nil
		def apply(t: T)(implicit context: ActorContext): ActorRef = factory(context, t)
}
