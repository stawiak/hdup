package com.outsmart

import dao.{Writer}
import measurement.Measurement
import org.joda.time.DateTime
import util.Util
import Util.withOpenClose
import akka.actor.ActorRef

/**
 * @author Vadim Bobrov
*/
class DataFiller(dataGen : DataGenerator, writer : Writer) {

  /**
   * fills the database with random crap
   * @param records number of records
   */
  def fillRandom(records : Int) {
    withOpenClose(writer) {

      for (i <- 0 until records) {
          writer.write(dataGen.getRandomMeasurement)
          if (i % 1000 == 0) println(i)
       }

    }

  }

  /**
   * fill the database for specific customer, location and wire
   * @param records number of records
   */
  def fill(customer:String, location:String, wireid:String, records : Int) {
    withOpenClose(writer) {

      for (i <- 0 until records) {
        writer.write(new Measurement(customer, location, wireid, i.asInstanceOf[Long], 8, 88, 888))
        if (i % 1000 == 0) println(i)
      }

    }
  }

  /**
   * fill the database with data spread evenly among all customers, locations and wires
   * from start time to end time every 5 minutes
   * @param start start time
   * @param end end time
   * @param value value to fill
   */
  def fillEven(start:DateTime, end:DateTime, value:Double) {
    withOpenClose(writer) {

      var counter = 0

      for(l <- start.getMillis until end.getMillis by 300000) {
        if ((l % 3600000) == 0)
          println("filling for " + new DateTime(l))

        for (i <- 0 until 20; j <- 0 until 2; k <- 0 until 300) {
            writer.write(new Measurement(dataGen.getCustomer(i), dataGen.getLocation(j), dataGen.getWireId(k), l, value, value, value))
            counter += 1
        }

      }

      println("generated " + counter + " msmts")

    }
  }

  /**
   * fill the database with data spread evenly among all customers, locations and wires
   * from start time to end time every 5 minutes
   * @param start start time
   * @param end end time
   * @param value value to fill
   */
  def fillEvenParallel(start:DateTime, end:DateTime, value:Double, actor: ActorRef) {
    var counter = 0

    for(l <- start.getMillis until end.getMillis by 300000) {
      if ((l % 3600000) == 0)
        println("filling for " + new DateTime(l))

      for (i <- 0 until 20; j <- 0 until 2; k <- 0 until 300) {
        actor ! new Measurement(dataGen.getCustomer(i), dataGen.getLocation(j), dataGen.getWireId(k), l, value, value, value)
        counter += 1
      }
    }

    println("generated " + counter + " msmts")
  }

  /**
   * fill the database with single value
   * @param value value to fill
   */
  def fillSimple(customer: String, location: String, wireid: String, timestamp: Long, value:Double) {
    withOpenClose(writer) {
          writer.write(new Measurement(customer, location, wireid, timestamp, value, value, value))
    }
  }

}
