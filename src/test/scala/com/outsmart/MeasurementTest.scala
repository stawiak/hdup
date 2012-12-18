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
      new MeasuredValue(120325, 1),
      new MeasuredValue(130325, 1),
      new MeasuredValue(120326, 1),
      new MeasuredValue(120325, 1),
      new MeasuredValue(215159991, 1)
    )) == (120000, 215160000))
  }

}
