package com.outsmart.unit

import org.scalatest.{FlatSpec}
import org.apache.commons.math3.analysis.interpolation.{SplineInterpolator, UnivariateInterpolator}
import org.scalatest.matchers.ShouldMatchers

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
}
