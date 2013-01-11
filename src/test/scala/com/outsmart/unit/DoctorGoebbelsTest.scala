package com.outsmart.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import com.typesafe.config.ConfigFactory
import com.outsmart.actor.{GracefulStop, LastMohican, FinalCountDown}
import akka.routing.RoundRobinRouter
import annotation.tailrec

/**
 * @author Vadim Bobrov
 */
class DoctorGoebbelsTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	val actorUnderTest = TestActorRef(new TestDoctorGoebbelsActor())


	"Doctor Goebbels actor" should "wait for the children to shutdown before shutting down" in {
		actorUnderTest !  Props(new TestChildActor()).withRouter(new RoundRobinRouter(3))
		actorUnderTest !  Props(new TestChildActor())

		actorUnderTest !  GracefulStop
	}

	override protected def afterAll() {
		system.awaitTermination()
	}

	case object WaitMessage
	class TestDoctorGoebbelsActor extends FinalCountDown with LastMohican {

		protected def receive: Receive = {

			case newChild : Props =>
				val newOne = context.actorOf(newChild)
				newOne ! WaitMessage
				newOne ! WaitMessage

			case GracefulStop =>
				onBlackSpot()
		}

	}

	class TestChildActor extends Actor with ActorLogging {

		protected def receive: Receive = {
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
