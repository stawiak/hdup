package com.os.actor.util

import akka.actor._
import com.os.measurement.Measurement
import akka.actor.Terminated
import akka.actor.DeadLetter
import com.os.actor.GracefulStop
import management.ManagementFactory
import javax.management.ObjectName
import com.os.util.JMXNotifier

/**
 * @author Vadim Bobrov
 */
trait DeadLetterListenerMBean
class DeadLetterListener extends JMXNotifier with Actor with ActorLogging with DeadLetterListenerMBean {
	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("com.os.chaos:type=DeadLetterListener,name=deadLetters"))
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
