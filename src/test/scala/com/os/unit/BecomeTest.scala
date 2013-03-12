package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.os.actor._
import concurrent.duration._
import com.os.util.{Pong, Ping}

/**
 * @author Vadim Bobrov
 */
class BecomeTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem())

	override def afterAll() {
		system.shutdown()
	}

	"become" should "take place immediately on exiting current message processing" in {
		val testActor = system.actorOf(Props(new BecomingActor))

		testActor ! Ping
		testActor ! Ping
		testActor ! Disable
		testActor ! Ping
		testActor ! Ping
		testActor ! Pong

		// Pings sent after Disable should be discarded
		receiveN(3, 1 second)
	}

	private class BecomingActor extends Actor {
		override def receive: Receive = {
			case Ping => testActor ! Ping
			case Disable => context.become(deaf)
		}

		def deaf: Receive = {
			case Pong => testActor ! Pong
		}
	}

}
