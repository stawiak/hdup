package com.os.actor.read

import akka.actor.{ActorLogging, Actor}
import com.os.dao.ReaderFactory
import management.ManagementFactory
import javax.management.ObjectName

/**
  * @author Vadim Bobrov
  */
trait ReadWorkerActorMBean
class ReadWorkerActor(val readerFactory : ReaderFactory) extends Actor with ActorLogging with ReadWorkerActorMBean {

	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("com.os.chaos:type=Reader,Reader=workers,name=\"" + self.path.name + "\""))
	val reader = readerFactory.createReader

	override def receive: Receive = {
		case request: ReadRequest =>
			sender ! reader.read(request)
	}

 }
