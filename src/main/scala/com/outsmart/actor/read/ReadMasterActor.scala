package com.outsmart.actor.read

import akka.actor._
import com.outsmart.measurement.{MeasuredValue, Rollup, Interpolated, Measurement}
import com.outsmart.Settings
import akka.routing.{RoundRobinRouter, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import com.outsmart.actor.{FinalCountDown, GracefulStop}
import com.outsmart.actor.write.WriteWorkerActor
import akka.actor.OneForOneStrategy
import akka.routing.Broadcast
import akka.pattern.ask
import akka.pattern.pipe
import concurrent.duration._
import concurrent.Future
import akka.util.Timeout


/**
 * @author Vadim Bobrov
 */
//case class RollupReadRequest(customer : String, location : String, override val periods : List[(String, String)]) extends ReadRequest(periods)
//case class MeasurementReadRequest(customer : String, location : String, override val periods : List[(String, String)]) extends ReadRequest(periods)

//case class ReadRequest(periods : List[(String, String)])

class ReadMasterActor extends FinalCountDown {

	import context._
	implicit val timeout = Timeout(10 seconds)

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


	private def getRouter(request : ReadRequest) : ActorRef = {

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

		case request : ReadRequest => {

			// we are sending Future(List(Future(List(Measurements))))
			//Future.sequence(periods map (period => (routers("") ? ScanRequest(customer, location, wireid, period)).mapTo[List[MeasuredValue]])) pipeTo sender

			val res = for {
				scanRequest <- request.scanRequests
				scannerResults = (getRouter(request) ? scanRequest).mapTo[List[MeasuredValue]]
				mv <- scannerResults
			} yield mv
			sender ! res


		}

		case GracefulStop =>
			log.debug("read master received graceful stop")
			routers.values foreach (_ ! Broadcast(GracefulStop))
			waitAndDie()
			children foreach (_ ! Broadcast(PoisonPill))

	}
}

