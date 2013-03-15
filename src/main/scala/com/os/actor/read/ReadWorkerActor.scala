package com.os.actor.read

import akka.actor.{ActorLogging, Actor}
import com.os.dao.ReaderFactory
import javax.management.ObjectName
import com.os.util.JMXActorBean

/**
  * @author Vadim Bobrov
  */
trait ReadWorkerActorMBean
class ReadWorkerActor(val readerFactory : ReaderFactory) extends Actor with ActorLogging with ReadWorkerActorMBean with JMXActorBean {

	override val jmxName = new ObjectName("com.os.chaos:type=Reader,Reader=workers,name=\"" + readerFactory.name + self.path.name + "\"")
	val reader = readerFactory.createReader

	override def receive: Receive = {
		case request: ReadRequest =>
			sender ! reader.read(request)
	}

 }
