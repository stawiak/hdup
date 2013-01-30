package com.os.actor

import com.os.measurement.Measurement
import javax.jms._
import com.os.measurement.MeasurementConverter._

/**
 * @author Vadim Bobrov
 */
class MessageListenerActor(host: String, queue: String) extends ActiveMQActor(host, queue) {

	val incomingHandler = context.system.actorFor("/user/incomingHandler")

	override def receive: Receive = {

		case msg : MapMessage => {
			val msmt: Measurement = msg
			incomingHandler ! msmt
		}

		case msg : TextMessage => {
			log.debug("received text {}", msg.getText)
		}

		case msg : Message => {
			log.debug("received message {}", msg)
		}

	}

}
