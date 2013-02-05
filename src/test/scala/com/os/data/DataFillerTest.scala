package com.os.data

import org.scalatest.FunSuite
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import com.os.DataFiller
import com.os.actor.write.WriteMasterActor
import com.os.measurement.EnergyMeasurement
import com.os.util.Timing
import com.os.actor.util.GracefulStop

/**
 * @author Vadim Bobrov
 */
class DataFillerTest extends FunSuite with Timing{


	test("even fill") {
		time(DataFiller.fillEven(new DateTime("2012-06-01"), new DateTime("2012-06-05"), 66))
	}


	test("even fill parallel") {
		val start = System.currentTimeMillis

		val config = ConfigFactory.load()
		val system = ActorSystem("test", config.getConfig("test"))
		val masterWriter = system.actorOf(Props[WriteMasterActor], name = "writeMaster")



		DataFiller.fillEvenParallel(new DateTime("2012-01-01"), new DateTime("2012-01-05"), 111, masterWriter)

		masterWriter ! GracefulStop

		system.awaitTermination()

		debug("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis - start) + " min")
	}

	test("even fill parallel simple") {
		val system = ActorSystem("test")

		val master = system.actorOf(Props(new WriteMasterActor()), name = "writeMaster")
		for (i <- 0 until 1000)
			master ! new EnergyMeasurement("b", "c", "d" + i, 2, 2)

		debug("flushing")
		master ! GracefulStop

		system.awaitTermination()

	}


	test("fill simple") {
		for(i <- 1 to 10000)
			DataFiller.fillSimple("customer1", "location1", "wireid1", i, i)
	}

}
