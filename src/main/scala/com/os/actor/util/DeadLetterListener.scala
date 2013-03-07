package com.os.actor.util

import akka.actor._
import com.os.measurement.Measurement
import akka.actor.Terminated
import akka.actor.DeadLetter
import com.os.actor.GracefulStop
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
		case DeadLetter(msmt: Measurement, sender: ActorRef, recipient: ActorRef) =>  {

			lostMsmt += 1
			log.debug("lost {}", lostMsmt )
			log.debug("dead letter: {} {} {}", sender, recipient, msmt)

			notify("deadletter", msmt.toString)
		}

		case GracefulStop =>
			self ! PoisonPill
	}

}
