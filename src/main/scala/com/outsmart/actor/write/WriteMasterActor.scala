package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.Writer
import akka.routing.{FromConfig}
import akka.actor.SupervisorStrategy.{ Resume, Restart, Escalate}
import akka.util.duration._
import akka.util.{Timeout, Duration}
import akka.dispatch.{Future, Await}
import akka.pattern.ask


/**
 * @author Vadim Bobrov
 */
case object Flush
case object StopWriter

class WriteMasterActor(val writerActorFactory : (String, Int) => Props = new DefaultWriterActorFactory, val batchSize: Int = Settings.BatchSize) extends Actor with ActorLogging {

	import context._
	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()
	var counter = 0

	//implicit val timeout = Timeout(20 seconds)



	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	private def getMsmtType(msmt : Measurement) : String = {
		if (msmt.tags == None)
			"msmt"
		else
			// so far only first tag is used
			msmt.tags.get.args(0)
	}

	private def getRouter(msmtType : String) : ActorRef = {
		if (!routers.contains(msmtType))
			routers += (msmtType -> actorOf(writerActorFactory(msmtType, batchSize), name = "workerRouter"))

		routers(msmtType)
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			counter += 1
			getRouter(getMsmtType(msmt)) ! msmt
		}

		case Flush =>  {
			log.debug("flush received at " + counter)
			routers.values foreach (_ ! Flush)
		}


		case StopWriter => {
			log.debug("write master received stop")

			// allow all children to finish processing

			sender ! StopWriter

			// stops this actor and all its supervised children
			stop(self)
		}

	}

}

class DefaultWriterActorFactory() {

	def apply(msmtType : String, batchSize: Int = Settings.BatchSize) : Props = {
		Props(new WriterActor(msmtType, batchSize)).withRouter(FromConfig())
	}

}