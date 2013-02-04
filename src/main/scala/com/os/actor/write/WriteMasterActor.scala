package com.os.actor.write

import akka.actor._
import com.os.measurement.{Rollup, Interpolated, Measurement}
import com.os.Settings
import akka.routing.{RoundRobinRouter, Broadcast, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import concurrent.duration._
import com.os.actor.util.{GracefulStop, FinalCountDown}


/**
 * @author Vadim Bobrov
 */
class WriteMasterActor extends FinalCountDown {

	import context._

	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()
	var counter = 0

	val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)
	var routerFactory : (ActorContext, String, Int) => ActorRef = {(actorContext : ActorContext, tableName : String, batchSize : Int) =>
		actorOf(Props(new WriteWorkerActor(tableName, batchSize)).withRouter(new RoundRobinRouter(3)).withDispatcher("akka.actor.deployment.workers-dispatcher"))
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	private def getRouter(msmt : Measurement) : ActorRef = {

		val (tableName, batchSize) = msmt match  {
			case imsmt : Interpolated => (Settings.MinuteInterpolatedTableName, Settings.DerivedDataBatchSize)
			case rmsmt : Rollup => (Settings.RollupTableName, Settings.DerivedDataBatchSize)
			case _ => (Settings.TableName, Settings.BatchSize)
		}

		if (!routers.contains(tableName)) {
			val newRouter = routerFactory(context, tableName, batchSize)
			routers += (tableName -> newRouter)
		}

		routers(tableName)
	}

	override def receive: Receive = {

		case msmt : Measurement => {
			counter += 1
			getRouter(msmt) ! msmt
		}

		case GracefulStop =>
			log.debug("write master received graceful stop")
			waitAndDie()
			children foreach (_ ! Broadcast(GracefulStop))
			children foreach (_ ! Broadcast(PoisonPill))


	}
}

