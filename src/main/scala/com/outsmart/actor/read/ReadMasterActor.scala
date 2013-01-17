package com.outsmart.actor.read

import akka.actor._
import com.outsmart.measurement.{MeasuredValue, Rollup, Interpolated, Measurement}
import com.outsmart.Settings
import akka.routing.{RoundRobinRouter, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import com.outsmart.actor.{DoctorGoebbels, GracefulStop}
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
case class RollupReadRequest(val customer : String, val location : String, val periods : List[(String, String)])
case class ReadRequest(val customer : String, val location : String, val wireid : String, val periods : List[(String, String)])
case class ScanRequest(val customer : String, val location : String, val wireid : String, val period : (String, String))
class ReadMasterActor extends DoctorGoebbels {

	import context._
	implicit val timeout = Timeout(10 seconds)
	type Result = List[MeasuredValue]

	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()

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
			case imsmt : Interpolated => (Settings.MinuteInterpolaedTableName, Settings.DerivedDataBatchSize)
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

		case ReadRequest(customer, location, wireid, periods) => {
			//getRouter(msmt) ! msmt
			//periods map (period => future { Scanner().scan(customer, location, wireid, new DateTime(period._1), new DateTime(period._2)) }) flatMap (_())

			// we are sending Future(List(Future(List(Measurements))))
			Future.sequence(periods map (period => (routers("") ? ScanRequest(customer, location, wireid, period)).mapTo[List[MeasuredValue]])) pipeTo sender






		}

		case GracefulStop =>
			log.debug("read master received graceful stop")
			routers.values foreach (_ ! Broadcast(GracefulStop))
			onBlackSpot()


	}
}

