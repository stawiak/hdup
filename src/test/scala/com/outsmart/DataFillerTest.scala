package com.outsmart

import dao.Writer
import org.scalatest.FunSuite
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

/**
 * @author Vadim Bobrov
*/
class DataFillerTest extends FunSuite {

  test("even fill") {
    val start = System.currentTimeMillis()
    val dataFiller = new DataFiller(new DataGenerator, Writer.create())
    dataFiller.fillEven(new DateTime("2012-05-01"), new DateTime("2012-05-31"), 555)
    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start) + " min")
  }


}
