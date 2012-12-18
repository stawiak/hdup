package com.outsmart

import actor.write.{Flush, Stop, WriteMasterActor}
import dao.Writer
import measurement.Measurement
import org.scalatest.FunSuite
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
*/
class DataFillerTest extends FunSuite {


  test("even fill") {
    val start = System.currentTimeMillis
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())
    dataFiller.fillEven(new DateTime("2012-06-01"), new DateTime("2012-06-05"), 66)
    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis - start) + " min")
  }


  test("even fill parallel") {
    val start = System.currentTimeMillis

    // Create an Akka system
    val config = ConfigFactory.load()
    val system = ActorSystem("test", config.getConfig("test"))

    val testDriverActor = system.actorOf(Props[TestDriverActor], name = "testdriver")
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())

    dataFiller.fillEvenParallel(new DateTime("2012-06-01"), new DateTime("2012-06-05"), 66, testDriverActor)

    testDriverActor ! Flush

    system.awaitTermination()
    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis - start) + " min")
  }

  test("even fill parallel simple") {
    val system = ActorSystem("test")

    val master = system.actorOf(Props[WriteMasterActor], name = "master")
    for (i <- 0 until 1000)
      master ! new Measurement("b", "c", "d" + i, 2, 2)

    println("flushing")
    master ! Flush

    system.awaitTermination()

  }


  test("fill simple") {
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())
    dataFiller.fillEvenSimple("customer1", "location1", "wireid1", 1, 1)
  }

}
