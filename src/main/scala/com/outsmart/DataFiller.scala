package com.outsmart

import org.joda.time.DateTime
import org.joda.time
import com.outsmart.Util.withOpenClose

/**
 * @author Vadim Bobrov
*/
class DataFiller(dataGen : DataGenerator, writer : WriterImpl) {

  /**
   * fills the database with random crap
   * @param records number of records
   */
  def fillRandom(records : Int) {
    withOpenClose(writer) {

      for (i <- 0 until records) {
          writer.write(dataGen.getRandomCustomer, dataGen.getRandomLocation, dataGen.getRandomWireId, i.asInstanceOf[Long], dataGen.getRandomMeasurement)
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
        writer.write(customer, location, wireid, i.asInstanceOf[Long], 888)
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
  def fillEven(start:DateTime, end:DateTime, value:Long) {
    withOpenClose(writer) {

      for(l <- start.getMillis until end.getMillis by 300000) {
        if (l % (3600000 * 24) == 0)
          println("filling for " + new DateTime(l))

        for (i <- 0 until 20; j <- 0 until 2; k <- 0 until 30)
              writer.write(dataGen.getCustomer(i), dataGen.getLocation(j), dataGen.getWireId(k), l, value)
      }

    }
  }

}
