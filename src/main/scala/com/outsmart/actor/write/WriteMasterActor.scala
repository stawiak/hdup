package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.{InterpolatedMeasurement, Measurement}
import com.outsmart.Settings
import akka.routing.FromConfig
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import akka.util.Duration


/**
 * @author Vadim Bobrov
 */
case object Flush
case object StopWriter

class WriteMasterActor(val writerActorFactory : String => Props = DefaultWriterActorFactory.create) extends Actor with ActorLogging {

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


	private def getRouter(msmt : Measurement) : ActorRef = {

		val tableName = msmt match  {
			case imsmt : InterpolatedMeasurement => Settings.MinuteInterpolaedTableName
			case _ => Settings.TableName
		}

		if (!routers.contains(tableName))
			routers += (tableName -> actorOf(writerActorFactory(tableName), name = "workerRouter"))

		routers(tableName)
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			counter += 1
			getRouter(msmt) ! msmt
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

object DefaultWriterActorFactory {

	def create(tableName : String) : Props = {
		Props(new WriterActor(tableName, Settings.BatchSize)).withRouter(FromConfig())
	}

}