package com.os.actor.write

import akka.actor._
import akka.routing.{RoundRobinRouter, DefaultResizer}
import akka.actor.SupervisorStrategy.{ Resume, Escalate}
import concurrent.duration._
import com.os.actor.util.{GroupMessage, FinalCountDown}
import com.os.measurement._
import com.os.util.{JMXActorBean, JMXNotifier, MappableCachingActorFactory, MappableActorCache}
import akka.actor.OneForOneStrategy
import com.os.dao.{TimeWindowState, WriterFactory}
import com.os.actor.{Disabled, Disable}
import javax.management.ObjectName
import java.util.UUID


/**
 * @author Vadim Bobrov
 */
trait WriteMasterActorMBean
class WriteMasterActor(mockFactory: Option[MappableActorCache[AnyRef, WriterFactory]] = None) extends JMXNotifier with FinalCountDown with WriteMasterActorMBean with JMXActorBean {

	import context._

	override val jmxName = new ObjectName("com.os.chaos:type=Writer,name=writeMaster")
	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)


	val defaultFactory = MappableCachingActorFactory[AnyRef, WriterFactory](
		WriterFactory(_),
		(factory: WriterFactory) =>
			actorOf(Props(new WriteWorkerActor(factory)).withRouter(new RoundRobinRouter(3)).withDispatcher("akka.actor.deployment.workers-dispatcher"))
	)

	val routers = mockFactory.getOrElse(defaultFactory)
	val disableGroup = GroupMessage(() => Disable())
	var reportDisabledId: UUID = _

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				=> Resume
			case _: Throwable                	=> Escalate
		}


	override def receive: Receive = {

		case msmt : Measurement =>
			routers(msmt) ! msmt

		case Disable(id) =>
			log.debug("received Disable")
			reportDisabledId = id

			become(collecting)
			children foreach (disableGroup.broadcast(_))
	}

	def collecting: Receive = {
		case Disabled(id) =>
			disableGroup.receive(id)

			// then become deaf
			if (disableGroup.isDone) {
				// listen to saving state only
				become(deaf)
				parent ! Disabled(reportDisabledId)
			}
	}

	def deaf: Receive = {
		case state: TimeWindowState =>
			routers(state) forward state
	}
}


