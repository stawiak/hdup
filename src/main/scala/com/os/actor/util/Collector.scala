package com.os.actor.util

import akka.actor.ActorRef
import java.util.UUID
import collection.mutable
import akka.routing.{CurrentRoutees, RouterRoutees}
import concurrent.Await
import akka.util.Timeout
import concurrent.duration._
import akka.pattern.ask


class Collector(implicit senderRef: ActorRef) {
	//TODO: consider using children paths instead of UUID
	val ids = mutable.Set[UUID]()
	implicit val timeout: Timeout = 10 seconds

	def send(actorRef: ActorRef, msg: Ideable) {
		actorRef.tell(msg, senderRef)
		ids += msg.id
	}

	def broadcast(router: ActorRef, messageFactory: () => Ideable) {
		val RouterRoutees(routees) = Await.result((router ? CurrentRoutees).mapTo[RouterRoutees], timeout.duration)

		routees foreach { routee =>
			val msg = messageFactory()
			routee.tell(msg, senderRef)
			ids += msg.id
		}
	}

	def collect(id: UUID) {
		ids += id
	}

	def receive(id: UUID) {
		ids -= id
	}

	def isDone:Boolean = ids.isEmpty

}

trait Ideable {
	val id: UUID

	def on(collector: Collector) {
		collector.collect(this.id)
	}

}

trait IdeableReceiver {
	this: ActorRef =>

	def !!!(msg: Ideable): Ideable = {
		this ! msg
		msg
	}

}
