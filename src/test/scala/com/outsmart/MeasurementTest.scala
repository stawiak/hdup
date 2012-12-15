package com.outsmart

import measurement.MeasuredValue
import org.scalatest.FunSuite
import MeasuredValue._

/**
 * @author Vadim Bobrov
*/
class MeasurementTest extends FunSuite {

  test("lower minute") {
    assert(minuteBoundary(Array(
      new MeasuredValue(1, 120325),
      new MeasuredValue(1, 130325),
      new MeasuredValue(1, 120326),
      new MeasuredValue(1, 120325),
      new MeasuredValue(1, 215159991)
    )) == (120000, 215160000))
  }

}
