package com.outsmart.actor.write

import akka.actor.{ActorLogging, Actor}
import com.outsmart.dao.Writer
import com.outsmart.util.Util
import Util.withOpenClose

/**
  * @author Vadim Bobrov
  */
class WriterActor(val writer : Writer) extends Actor with ActorLogging{

	protected def receive: Receive = {
		case work: WriteWork => {

			withOpenClose(writer) {
			 // this can fail anytime and should be retried
			 work.measurements foreach writer.write
			}

			sender ! WorkDone
		}
	}

 }
