package com.os.actor

import com.os.measurement.Measurement
import javax.jms._
import com.os.measurement.MeasurementConverter._
import akka.actor.PoisonPill

/**
 * @author Vadim Bobrov
 */
class MessageListenerActor(host: String, queue: String) extends ActiveMQActor(host, queue) with TopAware {

	val timeWindow = context.system.actorFor("/user/top/timeWindow")

	override def receive: Receive = {

		case msg : MapMessage => {
			val msmt: Option[Measurement] = msg
			if (msmt.isDefined)
				timeWindow ! msmt.get
		}

		case msg : TextMessage => {
			log.debug("received text {}", msg.getText)
			if (msg.getText.equalsIgnoreCase("stop"))
				top ! GracefulStop
		}

		case msg : Message => {
			log.debug("received message {}", msg)
		}

		case GracefulStop =>
			self ! PoisonPill

	}

}
