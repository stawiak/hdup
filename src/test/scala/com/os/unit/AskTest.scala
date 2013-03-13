package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import concurrent.duration._
import akka.routing.{RouterRoutees, CurrentRoutees, RoundRobinRouter}
import akka.pattern.ask
import com.os.TestActors
import akka.util.Timeout
import org.scalatest.matchers.ShouldMatchers
import concurrent.Await

/**
 * @author Vadim Bobrov
 */
class AskTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem())

	override def afterAll() {
		system.shutdown()
	}

	"asking an actor" should "result in a temporary actor as sender" in {
		val asked = system.actorOf(Props(new TestAskActor()), name = "actor1")
		val res = Await.result((asked ? "hello"), timeout.duration)
		res should  be ("akka://default/temp/$a")
	}

	class TestAskActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case x  =>
				println("from " + sender.path)
				sender ! sender.path.toString
		}
	}

}
