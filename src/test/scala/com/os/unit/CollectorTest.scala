package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import concurrent.duration._
import akka.routing.RoundRobinRouter
import com.os.TestActors
import akka.util.Timeout
import com.os.actor.util.Collector
import com.os.actor.{Disabled, Disable}
import java.util.UUID

/**
 * @author Vadim Bobrov
 */
class CollectorTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ImplicitSender with BeforeAndAfterAll {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem())

	override def afterAll() {
		system.shutdown()
	}

	"collecting over actors" should "fill when all replies are back" in {
		val testActor = system.actorOf(Props(new TestSenderActor))
		testActor ! Disable()
		receiveN(1, 1 second)
	}


	"collecting over routers" should "fill when all replies are back" in {
		val testActor = system.actorOf(Props(new TestRouterSenderActor))

		testActor ! Disable()
		receiveN(1, 1 second)
	}


	private class TestSenderActor extends TestBaseSenderActor {
		import context._

		val testActor1 = actorOf(Props(new TestWorkerActor))
		val testActor2 = actorOf(Props(new TestWorkerActor))
		val testActor3 = actorOf(Props(new TestWorkerActor))

		override def receive: Receive = {
			case Disable(id) =>
				toSendBack = sender
				reportDisabledId = id
				become(collecting)
				doneCollector.send(testActor1, Disable())
				doneCollector.send(testActor2, Disable())
				doneCollector.send(testActor3, Disable())

		}
	}

	private class TestRouterSenderActor extends TestBaseSenderActor {
		import context._

		val router = context.actorOf(Props(new TestWorkerActor()).withRouter(new RoundRobinRouter(4)))

		override def receive: Receive = {
			case Disable(id) =>
				toSendBack = sender
				reportDisabledId = id
				become(collecting)
				doneCollector.broadcast(router, () => Disable())
		}
	}

	private abstract class TestBaseSenderActor extends Actor with ActorLogging {
		val doneCollector = new Collector(self)
		var reportDisabledId: UUID = _
		var toSendBack: ActorRef = _

		def collecting: Receive = {
			case Disabled(id) =>
				println("received disabled from child " + sender.path)
				doneCollector.receive(id)
				if (doneCollector.isDone) {
					println("reporting disabled")
					toSendBack ! Disabled(reportDisabledId)
				}

		}

	}

	private class TestWorkerActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case Disable(id) =>
				sender ! Disabled(id)
		}
	}


}
