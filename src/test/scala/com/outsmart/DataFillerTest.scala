package com.outsmart

import actor.write.{Stop, MasterActor}
import dao.Writer
import org.scalatest.FunSuite
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import akka.actor.{Props, ActorSystem}

/**
 * @author Vadim Bobrov
*/
class DataFillerTest extends FunSuite {

/*
  test("even fill") {
    val start = System.currentTimeMillis
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())
    dataFiller.fillEven(new DateTime("2012-05-01"), new DateTime("2012-05-31"), 555)
    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis - start) + " min")
  }
*/

  test("even fill parallel") {
    val start = System.currentTimeMillis

    // Create an Akka system
    val system = ActorSystem("DataFillSystem")

    val master = system.actorOf(Props(new MasterActor(10)), name = "master")
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())

    dataFiller.fillEvenParallel(new DateTime("2012-04-01"), new DateTime("2012-04-30"), 444, master)

    master ! Stop
    system.shutdown()

    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis - start) + " min")

  }


}
