package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import com.typesafe.config.ConfigFactory
import akka.routing.{Broadcast, RoundRobinRouter}
import annotation.tailrec
import com.os.actor.util.{LastMohican, FinalCountDown}
import com.os.actor.GracefulStop
import com.os.TestActors
import com.os.util.{Pong, Ping}
import concurrent.duration._
import concurrent.{Future, Await, blocking}
import akka.pattern.GracefulStopSupport1


/**
 * @author Vadim Bobrov
 */
class FinalCountDownTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with TestActors {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override protected def afterAll() {
		system.shutdown()
	}

	"FinalCountDown actor" should "wait for the children routers to shutdown before shutting down" in {
		val actorUnderTest = TestActorRef(new TestFinalCountDownActor(true))
		actorUnderTest !  Props(new TestChildActor()).withRouter(new RoundRobinRouter(3))

		actorUnderTest !  GracefulStop
	}

	it should "wait for the children to shutdown before shutting down" in {
		val actorUnderTest = TestActorRef(new TestFinalCountDownActor(false))
		actorUnderTest !  Props(new TestChildActor())

		actorUnderTest !  GracefulStop
	}

	"syncKill" should "wait for the children to shutdown before proceeding" in {
		val actorUnderTest = system.actorOf(Props(new TestSyncKillActor), name = "parent")
		actorUnderTest !  GracefulStop
		actorUnderTest !  Ping
		expectNoMsg(2 seconds)
		receiveN(1, 5 seconds)
	}


	class TestSyncKillActor extends Actor with GracefulStopSupport1 with ActorLogging {
		val child = context.actorOf(Props(new SlowDieActor), name = "child")
		override def receive: Receive = {
			case GracefulStop =>
				val stopped = gracefulStop(child, 1 minute, GracefulStop)(context.system)
				Await.result(stopped, 1 minute)
				log.debug("proceeding")
			case x =>
				testActor ! x
		}

	}

	case object WaitMessage
	class TestFinalCountDownActor(broadcast: Boolean = false) extends FinalCountDown with LastMohican {

		override def receive: Receive = {

			case newChild : Props =>
				val newOne = context.actorOf(newChild)
				newOne ! WaitMessage
				newOne ! WaitMessage

			case GracefulStop =>
				waitAndDie()
				context.children foreach (_ ! (if (!broadcast) PoisonPill else Broadcast(PoisonPill)))
		}

	}

	class TestChildActor extends Actor with ActorLogging {

		override def receive: Receive = {
			case WaitMessage =>
				log.info("received wait message")
				waitMillis(2000)
				log.info("processed wait message")

			case x => log.info("received " + x)
		}

	}

	@tailrec
	final def waitMillis(millis : Int, start : Long = 0) {
		val begin = if (start == 0) System.currentTimeMillis() else start

		if (System.currentTimeMillis() - begin < millis)
			waitMillis(millis, begin)
	}

}
