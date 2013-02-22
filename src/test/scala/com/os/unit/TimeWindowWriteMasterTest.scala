package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.EnergyMeasurement
import akka.testkit._
import com.os.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import com.os.util.{ActorCache, TimeSource}
import com.os.actor.TopActor
import com.os.actor.util.DeadLetterListener
import scala.Some
import com.os.TestActors

/**
 * @author Vadim Bobrov
 */
class TimeWindowWriteMasterTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with OneInstancePerTest with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val testAggregatorFactory: ActorCache[(String, String)] = new ActorCache[(String, String)] {
		def getAll: Traversable[ActorRef] = Nil

		def apply(t: (String, String))(implicit context: ActorContext): ActorRef = context.actorOf(Props(new NoGoodnik))
	}

	system.actorOf(Props(new TopActor(
		Props(new NoGoodnik),
		Props(new TimeWindowActor(4 seconds,new TimeSource {}, Some(testAggregatorFactory))),
		Props(new NoGoodnik),
		Props(new TestActorForwarder),
		Props(new NoGoodnik),
		Props(new NoGoodnik),
		Props[DeadLetterListener],
		Props(new NoGoodnik)
	)), name = "top")
	// allow some time to bring up actors
	Thread.sleep(1000)
	val testTimeWindow = system.actorFor("/user/top/timeWindow")

	// OneInstancePerTest makes sure a system (constructor?) is created for EVERY test
	// this runs after EVERY test, not after all tests
	override def afterAll() {
		system.shutdown()
	}

	"time window" should "send expired measurements to write master" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", 0, 0)
		receiveN(1, 2 seconds)
	}

	it should "only store non-expired measurements" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", 0, 0)
		testTimeWindow !  new EnergyMeasurement("", "", "", 1, 0)
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis, 0)

		receiveN(3, 2 seconds)
		//TODO
		//testTimeWindow.underlyingActor.measurements.size should be (1)
	}

}
