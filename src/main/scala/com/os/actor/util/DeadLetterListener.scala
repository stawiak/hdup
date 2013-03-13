package com.os.actor.util

import akka.actor._
import akka.actor.Terminated
import akka.actor.DeadLetter
import com.os.actor.{Disabled, Disable}
import javax.management.ObjectName
import com.os.util.{JMXActorBean, JMXNotifier}

/**
 * @author Vadim Bobrov
 */
trait DeadLetterListenerMBean
class DeadLetterListener extends JMXNotifier with Actor with ActorLogging with DeadLetterListenerMBean with JMXActorBean {

	override val jmxName = new ObjectName("com.os.chaos:type=DeadLetterListener,name=deadLetters")
	var lostMsmt = 0

	def receive = {
		case Terminated => ;
		case DeadLetter(msg: Any, sender: ActorRef, recipient: ActorRef) =>  {
			log.debug("dead letter: {} {} {}", sender, recipient, msg)

			notify("deadletter", msg.toString)
		}

		case Disable(id) =>
			sender ! Disabled(id)
	}

}
