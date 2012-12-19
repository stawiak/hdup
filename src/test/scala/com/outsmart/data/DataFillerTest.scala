package com.outsmart.data

import org.scalatest.FunSuite
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import com.outsmart.{TestDriverActor, DataGenerator, DataFiller}
import com.outsmart.dao.Writer
import com.outsmart.actor.write.{Flush, WriteMasterActor}
import com.outsmart.measurement.Measurement

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

    dataFiller.fillEvenParallel(new DateTime("2012-01-01"), new DateTime("2012-01-05"), 111, testDriverActor)

    testDriverActor ! Flush

    system.awaitTermination()

    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis - start) + " min")
  }

  test("even fill parallel simple") {
    val system = ActorSystem("test")

    val master = system.actorOf(Props(new WriteMasterActor(Writer.create)), name = "master")
    for (i <- 0 until 1000)
      master ! new Measurement("b", "c", "d" + i, 2, 2, 2, 2)

    println("flushing")
    master ! Flush

    system.awaitTermination()

  }


  test("fill simple") {
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())
    for(i <- 1 to 10000)
      dataFiller.fillSimple("customer1", "location1", "wireid1", i, i)
  }

}
