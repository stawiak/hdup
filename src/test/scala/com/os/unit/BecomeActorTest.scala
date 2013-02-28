package com.os.unit

import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.typesafe.config.ConfigFactory
import com.os.util.{Pong, Ping}
import com.os.TestActors
import concurrent.duration._

/**
 * @author Vadim Bobrov
*/
class BecomeActorTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	var pingPongActor = TestActorRef(new PingPongActor())

	"Becomed actor" should "receive all messages sent after becoming" in {
		pingPongActor ! Pong
		pingPongActor ! Pong
		pingPongActor ! Pong
		Thread.sleep(1000)
		pingPongActor ! Ping

		pingPongActor ! Pong
		pingPongActor ! Pong
		pingPongActor ! Pong
		receiveN(3, 2 seconds)
	}

	class PingPongActor extends Actor {

		def receive: Receive = {
			case Ping =>
				context.become(pongReceive)
		}

		def pongReceive: Receive = { case Pong => testActor ! Pong}

	}

}