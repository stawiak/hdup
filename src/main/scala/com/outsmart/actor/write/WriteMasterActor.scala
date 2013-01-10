package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.{Interpolated, Measurement}
import com.outsmart.Settings
import akka.routing.{Broadcast, FromConfig}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import akka.util.Duration
import com.outsmart.actor.FinalCountDown


/**
 * @author Vadim Bobrov
 */
case object Flush
case object GracefulStop

class WriteMasterActor extends FinalCountDown {

	import context._
	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()
	var counter = 0

	var routerFactory : (ActorContext, String, Int) => ActorRef = {(actorContext : ActorContext, tableName : String, batchSize : Int) =>
		actorOf(Props(new WriterActor(tableName, batchSize)).withRouter(FromConfig()), name = "workerRouter")
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	private def getRouter(msmt : Measurement) : ActorRef = {

		val (tableName, batchSize) = msmt match  {
			case imsmt : Interpolated => (Settings.MinuteInterpolaedTableName, Settings.MinuteInterpolatedBatchSize)
			case _ => (Settings.TableName, Settings.BatchSize)
		}

		if (!routers.contains(tableName)) {
			val newRouter = routerFactory(context, tableName, batchSize)
			routers += (tableName -> newRouter)
		}

		routers(tableName)
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			counter += 1
			getRouter(msmt) ! msmt
		}

		case Flush => routers.values foreach (_ ! Flush)

		case GracefulStop =>
			log.debug("write master received graceful stop")
			onBlackSpot()

	}

}

