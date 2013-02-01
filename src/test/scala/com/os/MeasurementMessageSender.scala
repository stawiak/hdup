package com.os

import javax.jms._
import measurement.Measurement
import org.apache.activemq.ActiveMQConnectionFactory
import com.os.measurement.MeasurementConverter._

/**
 * @author Vadim Bobrov
 */
object MeasurementMessageSender {

	var connection: Connection = _
	implicit var session: Session = _
	var producer: MessageProducer = _
	var destination: Queue = _

	def start() {
		val connectionFactory = new ActiveMQConnectionFactory("tcp://node0:61616")
		connection = connectionFactory.createConnection()
		//connection.setExceptionListener(this);

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

		destination = session.createQueue("msmt")
		producer = session.createProducer(destination)

		connection.start()
	}

	def stop() {
		producer.close()
		session.close()
		connection.close()
	}

	def send(msmt: Measurement) {
		val msg: MapMessage = msmt
		producer.send(destination, msg)
	}

	def sendStop() {
		producer.send(destination, session.createTextMessage("stop"))
	}


}
