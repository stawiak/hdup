package com.outsmart

import org.scalatest.FunSuite
import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
class DataFillerTest extends FunSuite {

  test("even fill") {
    val start = System.currentTimeMillis()
    val dataFiller = new DataFiller(new DataGenerator, new WriterImpl)
    dataFiller.fillEven(new DateTime("2012-04-01"), new DateTime("2012-04-30"), 444)
    println("filled in " + (System.currentTimeMillis() - start)/60000 + " min")
  }


}
