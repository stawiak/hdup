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

	override def preRestart(reason: Throwable, message: Option[Any]) {
		// retry - forward is necessary to retain the master as sender
		// http://letitcrash.com/post/23532935686/watch-the-routees
		log.info("retrying instance hashcode # {}", this.hashCode())
		message foreach {self forward _ }
		log.info("restarting WorkerActor instance hashcode # {}", this.hashCode())
	}


 }
