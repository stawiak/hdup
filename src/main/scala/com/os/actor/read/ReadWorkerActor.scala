package com.os.actor.read

import akka.actor.{ActorLogging, Actor}
import com.os.dao.ReaderFactory

/**
  * @author Vadim Bobrov
  */
class ReadWorkerActor(val readerFactory : ReaderFactory) extends Actor with ActorLogging {

	val reader = readerFactory.createReader

	override def receive: Receive = {
		case request: ReadRequest =>
			sender ! reader.read(request)
	}

 }
