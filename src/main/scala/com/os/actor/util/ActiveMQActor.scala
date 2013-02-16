package com.os.actor.util

import akka.actor.{ActorLogging, Actor}
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms._
import concurrent.duration.Duration

/**
 * @author Vadim Bobrov
 */
case class Connect()
abstract class ActiveMQActor(host: String, port: Int, queue: String) extends Actor with ActorLogging with MessageListener {

	var connection: Connection = _
	var session: Session = _
	var consumer: MessageConsumer = _

	def onMessage(msg: Message) {
		self ! msg
	}

	override def receive: Receive = {
		// connect in receive because supervisor won't restart after failure in preStart
		case Connect =>
			val connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port)
			connection = connectionFactory.createConnection()
			//connection.setExceptionListener(this);

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
			val destination = session.createQueue(queue)
			consumer = session.createConsumer(destination)

			consumer.setMessageListener(this)
			connection.start()
	}

	override def preStart() {
		context.system.scheduler.scheduleOnce(Duration.Zero, self, Connect)
	}

	override def postStop() {
		consumer.close()
		session.close()
		connection.close()
	}


}
