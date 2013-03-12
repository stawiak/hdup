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
class RouteesTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem())

	override def afterAll() {
		system.shutdown()
	}

	"asking a router" should "for routees should return all routees" in {
		val router = system.actorOf(Props(new PingPonger()).withRouter(new RoundRobinRouter(4)))
		val RouterRoutees(routees) = Await.result((router ? CurrentRoutees).mapTo[RouterRoutees], timeout.duration)
		routees.size should be (4)
	}

}
