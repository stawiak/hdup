package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.{Interpolated, Measurement}
import com.outsmart.Settings
import akka.routing.FromConfig
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import akka.util.Duration
import com.outsmart.actor.service.TimeWindowActor


/**
 * @author Vadim Bobrov
 */
case object Flush
case object StopWriter

class WriteMasterActor extends Actor with ActorLogging {

	import context._
	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()
	var counter = 0

	var routerFactory : (ActorContext, String) => ActorRef = {(actorContext : ActorContext, tableName : String) =>
		actorOf(Props(new WriterActor(tableName, Settings.BatchSize)).withRouter(FromConfig()), name = "workerRouter")
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	private def getRouter(msmt : Measurement) : ActorRef = {

		val tableName = msmt match  {
			case imsmt : Interpolated => Settings.MinuteInterpolaedTableName
			case _ => Settings.TableName
		}

		if (!routers.contains(tableName))
			routers += (tableName -> routerFactory(context, tableName))

		routers(tableName)
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			counter += 1
			getRouter(msmt) ! msmt
		}

		case Flush =>  {
			//log.debug("flush received at " + counter)
			routers.values foreach (_ ! Flush)
		}


		case StopWriter => {
			//log.debug("write master received stop")

			// allow all children to finish processing

			sender ! StopWriter

			// stops this actor and all its supervised children
			stop(self)
		}

	}

	object DefaultWriterFactory {

		def get(context : ActorContext, tableName : String) : ActorRef = {
			context.actorOf(Props(new WriterActor(tableName, Settings.BatchSize)).withRouter(FromConfig()), name = "workerRouter")
		}

	}
}

