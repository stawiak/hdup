package com.os.actor.read

import akka.actor._
import com.os.measurement.MeasuredValue
import com.os.Settings
import akka.routing.{RoundRobinRouter, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import akka.actor.OneForOneStrategy
import akka.routing.Broadcast
import akka.pattern.ask
import akka.pattern.pipe
import concurrent.duration._
import concurrent.Future
import akka.util.Timeout
import com.os.actor.util.{GracefulStop, FinalCountDown}


/**
 * @author Vadim Bobrov
 */
class ReadMasterActor extends FinalCountDown {

	import context._
	implicit val timeout: Timeout = 10 seconds

	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()

	val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)
	var routerFactory : (ActorContext, String) => ActorRef = {(actorContext : ActorContext, tableName : String) =>
		actorOf(Props(new ReadWorkerActor(tableName)).withRouter(new RoundRobinRouter(3)).withDispatcher("akka.actor.deployment.workers-dispatcher"))
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	private def getRouter(request : AnyRef) : ActorRef = {

		val tableName = request match  {
			case RollupReadRequest => Settings.RollupTableName
			case _ => Settings.TableName
		}

		if (!routers.contains(tableName)) {
			val newRouter = routerFactory(context, tableName)
			routers += (tableName -> newRouter)
		}

		routers(tableName)
	}

	override def receive: Receive = {

		case request : MeasurementReadRequest => {
			log.debug("received measurement read request {}", request)
			Future.traverse(request.scanRequests)(req => (getRouter(request) ? req).mapTo[Iterable[MeasuredValue]]).map(_.flatten) pipeTo sender
		}

		case request : RollupReadRequest => {

			//val f: Future[List[List[MeasuredValue]]] = Future.sequence(request.scanRequests.map(getRouter(request) ? _).map(_.mapTo[List[MeasuredValue]]))
			//f.map(_.flatMap(identity)) pipeTo sender

			log.debug("received rollup read request {}", request)
			Future.traverse(request.scanRequests)(req => (getRouter(request) ? req).mapTo[Iterable[MeasuredValue]]).map(_.flatten) pipeTo sender

		}

		case GracefulStop =>
			log.debug("read master received graceful stop")
			routers.values foreach (_ ! Broadcast(GracefulStop))
			waitAndDie()
			children foreach (_ ! Broadcast(PoisonPill))

	}
}

