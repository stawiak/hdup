package com.outsmart.unit

import org.scalatest.FunSuite
import com.outsmart.measurement.MeasuredValue._
import scala.Array
import com.outsmart.measurement.MeasuredValue

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
    )) === (120000, 215160000))
  }

}
