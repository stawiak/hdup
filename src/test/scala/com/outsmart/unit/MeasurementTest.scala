package com.outsmart.unit

import org.scalatest.{FlatSpec}
import com.outsmart.measurement.MeasuredValue._
import scala.Array
import com.outsmart.measurement.MeasuredValue
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
*/
class MeasurementTest extends FlatSpec with ShouldMatchers {

  "Lower minute" should  "be lower minute boundary" in {
    minuteBoundary(Array(
      new MeasuredValue(120325, 1, 1, 1),
      new MeasuredValue(130325, 1, 1, 1),
      new MeasuredValue(120326, 1, 1, 1),
      new MeasuredValue(120325, 1, 1, 1),
      new MeasuredValue(215159991, 1, 1, 1)
    )) should be (120000, 215160000)
  }

}
