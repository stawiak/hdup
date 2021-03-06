package com.os.actor.read

import akka.actor._
import akka.routing.{RoundRobinRouter, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import concurrent.duration._
import akka.util.Timeout
import com.os.Settings
import org.joda.time.Interval
import com.os.util.{JMXActorBean, MappableActorCache, MappableCachingActorFactory}
import akka.actor.OneForOneStrategy
import com.os.actor.{Disabled, Disable}
import javax.management.ObjectName
import com.os.dao.read.ReaderFactory
import com.os.dao.clwt.CLWTReaderFactory


/**
 * @author Vadim Bobrov
 */
sealed abstract class ReadRequest
case class MeasurementReadRequest(tableName: String, customer: String, location: String, wireid: String, period: Interval) extends ReadRequest
case class RollupReadRequest(customer: String, location: String, period: Interval) extends ReadRequest

case class LoadState(id: AnyRef) extends ReadRequest

trait ReadMasterActorMBean
class ReadMasterActor(mockFactory: Option[MappableActorCache[ReadRequest, ReaderFactory]] = None) extends Actor with ActorLogging with ReadMasterActorMBean with JMXActorBean {

	import context._
	override val jmxName = new ObjectName("com.os.chaos:type=Reader,name=readMaster")
	implicit val timeout: Timeout = Settings().ReadTimeout


	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)

	val defaultFactory = MappableCachingActorFactory[ReadRequest, ReaderFactory](
		CLWTReaderFactory(_),
		(factory: ReaderFactory) =>
			actorOf(
				Props(new ReadWorkerActor(factory)).withRouter(new RoundRobinRouter(3)).withDispatcher("akka.actor.deployment.workers-dispatcher")
			)
	)

	val routers = mockFactory.getOrElse(defaultFactory)

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}

	override def receive: Receive = {

		case request : MeasurementReadRequest =>
			log.debug("received measurement read request {}", request)
			routers(request) forward request


		case request : RollupReadRequest =>

			//val f: Future[List[List[MeasuredValue]]] = Future.sequence(request.scanRequests.map(getRouter(request) ? _).map(_.mapTo[List[MeasuredValue]]))
			//f.map(_.flatMap(identity)) pipeTo sender
			log.debug("received rollup read request {}", request)
			routers(request) forward request

		case request @ LoadState(_) =>
			log.debug("read master received LoadState")
			routers(request) forward request

		case Disable(id) =>
			sender ! Disabled(id)
	}
}

