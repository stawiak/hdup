package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import concurrent.duration._
import akka.routing.{Broadcast, RoundRobinRouter}
import akka.pattern.ask
import akka.pattern.pipe
import com.os.util.Ping
import com.os.TestActors
import akka.util.Timeout

/**
 * @author Vadim Bobrov
 */
class AskRouterTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ImplicitSender with BeforeAndAfterAll {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem())

	override def afterAll() {
		system.shutdown()
	}

	"asking a router" should "produce a single result" in {
		val testActor = system.actorOf(Props(new TestMasterActor))

		testActor ! Ping
		receiveN(1, 1 second)
	}

	private class TestMasterActor extends Actor with ActorLogging {
		import context.dispatcher
		val router = context.actorOf(Props(new PingPonger()).withRouter(new RoundRobinRouter(3)))

		override def receive: Receive = {
			case Ping =>
				router ? Broadcast(Ping) pipeTo sender
		}
	}

}
