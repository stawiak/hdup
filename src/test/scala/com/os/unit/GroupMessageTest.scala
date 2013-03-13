package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import concurrent.duration._
import akka.routing.RoundRobinRouter
import com.os.TestActors
import akka.util.Timeout
import com.os.actor.util.GroupMessage

/**
 * @author Vadim Bobrov
 */
class GroupMessageTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ImplicitSender with BeforeAndAfterAll {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem())

	case class TestMessage()

	override def afterAll() {
		system.shutdown()
	}

	"collecting over actors" should "fill when all replies are back" in {
		val testActor = system.actorOf(Props(new TestSenderActor))
		testActor ! TestMessage()
		receiveN(1, 1 second)
	}


	"collecting over routers" should "fill when all replies are back" in {
		val testActor = system.actorOf(Props(new TestRouterSenderActor))

		testActor ! TestMessage()
		receiveN(1, 1 second)
	}


	private class TestSenderActor extends TestBaseSenderActor {
		import context._

		val testActor1 = actorOf(Props(new TestWorkerActor))
		val testActor2 = actorOf(Props(new TestWorkerActor))
		val testActor3 = actorOf(Props(new TestWorkerActor))

		override def receive: Receive = {
			case TestMessage() =>
				toSendBack = sender
				become(collecting)
				testActor1 ! disableGroup.newMessage()
				testActor2 ! disableGroup.newMessage()
				testActor3 ! disableGroup.newMessage()

		}
	}

	private class TestRouterSenderActor extends TestBaseSenderActor {
		import context._

		val router = context.actorOf(Props(new TestWorkerActor()).withRouter(new RoundRobinRouter(4)))

		override def receive: Receive = {
			case TestMessage() =>
				toSendBack = sender
				become(collecting)
				disableGroup.broadcast(router)
		}
	}

	private abstract class TestBaseSenderActor extends Actor with ActorLogging {
		val disableGroup = GroupMessage(() => TestMessage())
		var toSendBack: ActorRef = _

		def collecting: Receive = {
			case x @ TestMessage() =>
				println("received disabled from child " + sender.path)
				disableGroup.receive(x)
				if (disableGroup.isDone) {
					println("reporting disabled")
					toSendBack ! x
				}

		}

	}

	private class TestWorkerActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case x @ TestMessage() =>
				sender ! x
		}
	}


}
