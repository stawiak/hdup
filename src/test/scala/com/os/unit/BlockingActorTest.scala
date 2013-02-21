package com.os.unit

import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.typesafe.config.ConfigFactory
import scala._
import com.os.actor.read.MeasurementReadRequest
import concurrent.Await
import org.joda.time.Interval
import concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import com.os.util.{Pong, Ping}
import com.os.TestActors

/**
 * @author Vadim Bobrov
*/
class BlockingActorTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	var blockingActor = TestActorRef(new BlockingActor())

	"A blocking actor" should "block on its child worker" in {
		blockingActor ! MeasurementReadRequest("", "", "", "", new Interval(0,1))
		blockingActor ! Ping
		expectNoMsg(1 second)
	}

	class BlockingActor extends Actor {
		implicit val timeout: Timeout = 3 seconds
		private val slowWorker: ActorRef = context.actorOf(Props[SlowActor])

		private final def blockingReceive: Receive = {
			case _ =>
				sender ! Await.result(slowWorker ? Ping, 3 seconds)
		}

		private final def pingPong: Receive = {	case Ping => sender ! Pong }
		override def receive = pingPong orElse blockingReceive
	}

}