package com.outsmart.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.outsmart.measurement.{Interpolated, Measurement}
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.outsmart.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import akka.util.duration._
import com.outsmart.actor.DoctorGoebbels
import akka.actor.OneForOneStrategy
import com.outsmart.actor.write.{GracefulStop, WriterActor}
import akka.routing.{Broadcast, FromConfig}
import akka.util.Duration
import akka.actor.SupervisorStrategy.{Escalate, Resume}
import com.outsmart.Settings

/**
 * @author Vadim Bobrov
 */
class DoctorGoebbelsTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.awaitTermination()
	}

	val testTimeWindow = TestActorRef(new TestDoctorGoebbelsActor())

	"Doctor Goebbels actor" should "wait for the children to shutdown before shutting down" in {
		val testRouter1 = TestProbe()
		val testRouter2 = TestProbe()
		val testRouter3 = TestProbe()

		testTimeWindow !  testRouter1.ref
		testTimeWindow !  testRouter2.ref
		testTimeWindow !  testRouter3.ref

		testTimeWindow !  GracefulStop

		testRouter1.expectMsg(Broadcast(PoisonPill))
		testRouter2.expectMsg(Broadcast(PoisonPill))
		testRouter3.expectMsg(Broadcast(PoisonPill))
		//TestInterpolatorFactory.interpolator.expectNoMsg
	}


	class TestDoctorGoebbelsActor extends Actor with DoctorGoebbels with ActorLogging {

		protected def receive: Receive = {

			//case NewRouter => addRouter(context.actorOf(Props(new TestProbe()).withRouter(FromConfig()), name = "workerRouter"))
			case newRouter : ActorRef => addRouter(newRouter)

			case GracefulStop => onBlackMark()

		}

	}

}
