package com.os.actor.util

import akka.actor.{ActorLogging, Actor}
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms._
import concurrent.duration._

/**
 * @author Vadim Bobrov
 */
case object Connect
case object Disconnect
abstract class ActiveMQActor(host: String, port: Int, queue: String) extends Actor with ActorLogging with MessageListener {

	import context._
	var connection: Connection = _
	var session: Session = _
	var consumer: MessageConsumer = _

	def onMessage(msg: Message) {
		self ! msg
	}

	override def receive: Receive = {
		// connect in receive because supervisor won't restart after failure in preStart
		case Connect =>
			log.debug("attempting connection to ActiveMQ")
			val connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port)
			connection = connectionFactory.createConnection()
			//connection.setExceptionListener(this);

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
			val destination = session.createQueue(queue)
			consumer = session.createConsumer(destination)

			consumer.setMessageListener(this)
			connection.start()
			log.debug("connected to ActiveMQ")

		case Disconnect =>
			close()
	}

	override def preStart() {
		log.debug("ActiveMQ listener starting")
		system.scheduler.scheduleOnce(1 second, self, Connect)
		super.preStart()
	}

	private def close() {
		log.debug("closing connection to ActiveMQ")
		consumer.close()
		session.close()
		connection.close()
		log.debug("ActiveMQ connection closed")
	}

	override def postStop() {
		try {
			close()
		} catch {
			// ignore errors on close
			case _: Throwable =>
		}

		super.postStop()
	}


}
