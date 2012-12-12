package com.outsmart

import org.scalatest.FunSuite
import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
class DataFillerTest extends FunSuite {

  test("even fill") {
    val dataFiller = new DataFiller(new DataGenerator, new WriterImpl)
    dataFiller.fillEven(new DateTime("2012-01-01"), new DateTime("2012-01-02"), 111)
  }


}
