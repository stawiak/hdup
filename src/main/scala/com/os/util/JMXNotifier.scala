package com.os.util

import javax.management.{ObjectName, Notification, NotificationBroadcasterSupport}
import management.ManagementFactory
import akka.actor.Actor

/**
 * @author Vadim Bobrov
 */
trait JMXNotifier extends NotificationBroadcasterSupport {
	var notificationSequence:Int = 0

	def notify(notificationType: String, msg: String) {
		sendNotification(new Notification(notificationType, this, notificationSequence, msg))
		notificationSequence += 1
	}

}

trait JMXActorBean extends Actor {
	val jmxName: ObjectName

	abstract override def preStart() {
		ManagementFactory.getPlatformMBeanServer.registerMBean(this, jmxName)
		super.preStart()
	}


	abstract override def postStop() {
		ManagementFactory.getPlatformMBeanServer.unregisterMBean(jmxName)
		super.postStop()
	}

}
