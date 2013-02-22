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
import com.os.TestActors
import scala.Predef._
import scala.Some

/**
 * @author Vadim Bobrov
 */
class TimeWindowAggregatorTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with OneInstancePerTest with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val testAggregatorFactory: ActorCache[(String, String)] = new ActorCache[(String, String)] {
		def getAll: Traversable[ActorRef] = Nil

		def apply(actorContext: ActorContext, t: (String, String)): ActorRef = actorContext.actorOf(Props(new TestActorForwarder))
	}

	system.actorOf(Props(new TopActor(
		Props(new NoGoodnik),
		Props(new TimeWindowActor(4 seconds,new TimeSource {}, Some(testAggregatorFactory))),
		Props(new NoGoodnik),
		Props(new NoGoodnik),
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

	"time window" should "send nothing before expiration window expire" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis, 0)
		expectNoMsg(2 seconds)
	}

	it should "send old to interpolator after expiration window expire" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis, 0)
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis + 1, 0)
		expectNoMsg(1 seconds)
		Thread.sleep(5000)
		receiveN(2, 2 seconds)
	}

	it should "not send new to interpolator after expiration window expire" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis, 0)
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis + 1, 0)
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis + 2, 0)
		expectNoMsg(10 milliseconds)
		Thread.sleep(5000)
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis + 3, 0)
		testTimeWindow !  new EnergyMeasurement("", "", "", System.currentTimeMillis + 4, 0)
		receiveN(3, 2 seconds)
	}

}
