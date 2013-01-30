package com.os.actor

import akka.actor.{ActorLogging, Actor}
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms._

/**
 * @author Vadim Bobrov
 */
abstract class ActiveMQActor(host: String, queue: String) extends Actor with ActorLogging with MessageListener {

	var connection: Connection = _
	var session: Session = _
	var consumer: MessageConsumer = _

	def onMessage(msg: Message) {
		self ! msg
	}


	override def preStart() {
		val connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":61616")
		connection = connectionFactory.createConnection()
		//connection.setExceptionListener(this);

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
		val destination = session.createQueue(queue)
		consumer = session.createConsumer(destination)

		consumer.setMessageListener(this)
		connection.start()
	}

	override def postStop() {
		consumer.close()
		session.close()
		connection.close()
	}


}
