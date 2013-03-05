package com.os.util

import javax.management.{Notification, NotificationBroadcasterSupport}

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
