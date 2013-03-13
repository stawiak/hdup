package com.os.actor.util

import akka.actor.ActorRef
import java.util.UUID
import collection.mutable
import akka.routing.{CurrentRoutees, RouterRoutees}
import concurrent.Await
import akka.util.Timeout
import concurrent.duration._
import akka.pattern.ask


object GroupMessage {
	def apply(messageFactory:() => { val id: UUID }): GroupMessage = new GroupMessage(messageFactory)
}

/**
 * Usage:
 * <pre>
 * {@code
 *
 * val disableGroup = GroupMessage(() => Disable())
 * // regular actor
 * child ! disableGroup.newMessage()
 *
 * // router
 * disableGroup.broadcast(router)
 *
 * case x @ Disable() =>
 * 		disableGroup.receive(x)
 * 		if(disableGroup.isDone)
 * 			doSomething
 * }
 * </pre>
 * @param messageFactory new message creator
 */
class GroupMessage(messageFactory:() => { val id: UUID }) {

	implicit val timeout: Timeout = 10 seconds

	val messages = mutable.Set[UUID]()

	def newMessage(): Any = {
		val msg = messageFactory()
		messages += msg.id
		msg
	}


	def broadcast(router: ActorRef)(implicit senderRef: ActorRef) {
		val RouterRoutees(routees) = Await.result((router ? CurrentRoutees).mapTo[RouterRoutees], timeout.duration)
		routees foreach { _.tell(newMessage(), senderRef) }
	}

	def receive(msg: { val id: UUID } ) {
		messages -= msg.id
	}

	def receive(id: UUID) {
		messages -= id
	}

	def isDone:Boolean = messages.isEmpty

}




