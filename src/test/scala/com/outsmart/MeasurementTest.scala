package com.outsmart

import org.scalatest.FunSuite
import com.outsmart.Measurement._

/**
 * @author Vadim Bobrov
*/
class MeasurementTest extends FunSuite {

  test("lower minute") {
    assert(minuteBoundary(Array(
      new Measurement(1, 120325),
      new Measurement(1, 130325),
      new Measurement(1, 120326),
      new Measurement(1, 120325),
      new Measurement(1, 215159991)
    )) == (120000, 215160000))
  }

}
