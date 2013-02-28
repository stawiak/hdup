package com.os.actor.write

import akka.actor._
import akka.routing.{RoundRobinRouter, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import concurrent.duration._
import com.os.actor.util.{GracefulStop, FinalCountDown}
import com.os.measurement._
import com.os.util.{MappableCachingActorFactory, MappableActorCache}
import akka.actor.OneForOneStrategy
import akka.routing.Broadcast
import com.os.dao.{WriterFactory, AggregatorState}
import concurrent.Future
import akka.pattern.pipe


/**
 * @author Vadim Bobrov
 */

class WriteMasterActor(mockFactory: Option[MappableActorCache[AnyRef, WriterFactory]] = None) extends FinalCountDown {

	import context._
	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)


	val defaultFactory = MappableCachingActorFactory[AnyRef, WriterFactory](
		WriterFactory(_),
		(factory: WriterFactory) =>
			actorOf(Props(new WriteWorkerActor(factory)).withRouter(new RoundRobinRouter(3)).withDispatcher("akka.actor.deployment.workers-dispatcher"))
	)

	val routers = if (mockFactory.isEmpty) defaultFactory else mockFactory.get


	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				=> Resume
			case _: Throwable                	=> Escalate
		}



	override def receive: Receive = {

		case msmt : Measurement =>
			routers(msmt) ! msmt

		case state: Future[AggregatorState] =>
			state pipeTo routers(state)

		case GracefulStop =>
			log.debug("write master received graceful stop")
			waitAndDie()
			children foreach (_ ! Broadcast(GracefulStop))
			children foreach (_ ! Broadcast(PoisonPill))
	}
}

