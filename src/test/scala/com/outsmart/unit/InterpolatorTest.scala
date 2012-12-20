package com.outsmart.unit

import org.scalatest.{FlatSpec}
import org.apache.commons.math3.analysis.interpolation.{SplineInterpolator, UnivariateInterpolator}
import org.scalatest.matchers.ShouldMatchers
import com.outsmart.measurement.{TimedValue, Interpolator}

/**
 * @author Vadim Bobrov
*/
class InterpolatorTest extends FlatSpec with ShouldMatchers {

  "Interpolation" should "be fast" in {
    val start = System.currentTimeMillis
    val x = Array( 0.0, 1.0, 2.0, 3.0, 4.0, 5.0,5.5, 6.0, 7.0,8.0, 9.0, 10.0,11.0, 12.0, 13.0, 14.0, 15.0, 16.0 )
    val y = Array( 1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0 )


    val interpolator = new SplineInterpolator()
    val function = interpolator.interpolate(x, y)

    val interpolationX = 0.5
    val interpolatedY = function.value(interpolationX)

    for(i <- 0 to 100)
      println(function.value(i * 0.001))

    println((System.currentTimeMillis() - start) + "ms")
    //assert(interpolatedY > 0)
  }

  "linear interpolation" should "be correct" in {
    Interpolator.linearInterpolate(0.5, 0, 0, 1, 1) should be (0.5)
    Interpolator.linearInterpolate(0.5, 0, 1, 1, 1) should be (1)
    Interpolator.linearInterpolate(0.5, 0, 2, 1, 1) should be (1.5)
  }

  "linear interpolation" should "fail when not a function" in {
    evaluating(
      Interpolator.linearInterpolate(0.5, 1, 0, 1, 1)
    ) should produce [AssertionError]
  }

  "intersection" should "return correct value" in {
    Interpolator.findIntersection(0, 1, 2, 2, 5, 5, 6, 7) should be ((4, 3))
  }

  "intersection" should "return (0, 0) when parallel" in {
    Interpolator.findIntersection(0, 2, 2, 2, 5, 5, 6, 5) should be ((0, 0))
  }

  "intersection" should "return (-1, -1) when intersection outside of period" in {
    Interpolator.findIntersection(0, 1, 2, 2, 5, 7, 6, 5) should be ((-1, -1))
  }

  "intersection" should "fail when arguments not ordered" in {
    evaluating(
      Interpolator.findIntersection(3, 1, 2, 2, 5, 5, 6, 7)
    ) should produce [AssertionError]
  }

  "interpolation" should "fail when fewer than 4 measurements sent" in {
    val arr = Array(new TimedValue(1,1), new TimedValue(1,1), new TimedValue(1,1))

    evaluating(
      Interpolator.minuteBoundaryBilinear(arr)
    ) should produce [AssertionError]
  }

  "interpolation" should "work with 4 measurements" in {
    val arr = Array(new TimedValue(119996,1), new TimedValue(119998,2), new TimedValue(120001,5), new TimedValue(120002,7))

    Interpolator.minuteBoundaryBilinear(arr) should have size (1)
    Interpolator.minuteBoundaryBilinear(arr)(0).timestamp should be (120000)
    Interpolator.minuteBoundaryBilinear(arr)(0).value should be (3)
  }

}
