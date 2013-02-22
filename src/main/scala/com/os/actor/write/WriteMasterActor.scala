package com.os.actor.write

import akka.actor._
import akka.routing.{RoundRobinRouter, Broadcast, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import concurrent.duration._
import com.os.actor.util.{SettingsUse, GracefulStop, FinalCountDown}
import com.os.measurement._
import com.os.Settings
import com.os.util.{CachingActorFactory, ActorCache}
import akka.routing.Broadcast
import akka.actor.OneForOneStrategy
import com.os.actor.service.AggregatorActor


/**
 * @author Vadim Bobrov
 */

class WriteMasterActor(mockFactory: Option[ActorCache[(String, Int)]] = None) extends FinalCountDown with SettingsUse {

	import context._

	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var routers = Map[String, ActorRef]()

	val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)


	val defaultFactory = CachingActorFactory[(String, Int)]((tableBatch: (String, Int)) =>
		actorOf(Props(new WriteWorkerActor(tableBatch._1, tableBatch._2)).withRouter(new RoundRobinRouter(3)).withDispatcher("akka.actor.deployment.workers-dispatcher")))
	//TODO val aggregators: ActorCache[(String, Int)] = if (mockFactory.isEmpty) defaultFactory else mockFactory.get

	val routerFactory = if (mockFactory.isEmpty) defaultFactory else mockFactory.get

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				=> Resume
			case _: Throwable                	=> Escalate
		}


	private def getRouter(msmt : Measurement) : ActorRef = {

		val (tableName, batchSize) = msmt match  {
			case msmt: Interpolated => (Settings.MinuteInterpolatedTableName, settings.DerivedDataBatchSize)
			case msmt: Rollup => (Settings.RollupTableName, settings.DerivedDataBatchSize)
			case msmt: EnergyMeasurement => (Settings.TableName, settings.BatchSize)
			case msmt: CurrentMeasurement => (Settings.CurrentTableName, settings.BatchSize)
			case msmt: VampsMeasurement => (Settings.VampsTableName, settings.BatchSize)
		}

		if (!routers.contains(tableName)) {
			val newRouter = routerFactory((tableName, batchSize))
			routers += (tableName -> newRouter)
		}

		routers(tableName)
	}

	override def receive: Receive = {

		case msmt : Measurement =>
			getRouter(msmt) ! msmt


		case GracefulStop =>
			log.debug("write master received graceful stop")
			waitAndDie()
			children foreach (_ ! Broadcast(GracefulStop))
			children foreach (_ ! Broadcast(PoisonPill))
	}
}

