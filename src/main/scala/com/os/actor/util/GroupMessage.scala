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
	def apply(messageFactory:() => Any): GroupMessage = new GroupMessage(messageFactory)
}

/**
 * Usage:
 * <pre>
 * {@code
 *
 * val disableGroup = GroupMessage(() => Disable())
 * // regular actor
 * child ! disableGroup.newMessage()  // new message is added to set
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
 * Prerequisites:
 * 		message type must be Set-able (i.e. equals and hash must work, e.g. be a case class)
 * 		must be replied with the same message i.e. case x => sender ! x
 * @param messageFactory new message creator
 */
class GroupMessage(messageFactory:() => Any) {

	implicit val timeout: Timeout = 10 seconds

	val messages = mutable.Set[Any]()

	def newMessage(): Any = {
		val msg = messageFactory()
		messages += msg
		msg
	}


	def broadcast(router: ActorRef)(implicit senderRef: ActorRef) {
		val RouterRoutees(routees) = Await.result((router ? CurrentRoutees).mapTo[RouterRoutees], timeout.duration)
		routees foreach { _.tell(newMessage(), senderRef) }
	}

	def receive(msg: Any) {
		messages -= msg
	}

	def isDone:Boolean = messages.isEmpty

}




