package com.outsmart.unit

import org.scalatest.{FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.outsmart.measurement.{TimedValue, Interpolator}
import util.Random
import actors.Futures._

/**
 * @author Vadim Bobrov
*/
class InterpolatorTest extends FlatSpec with ShouldMatchers {

  import Interpolator._

  val MeasurementsPerDaySingleWire = 140040

  "linear interpolation" should "be correct" in {
    linearInterpolate(0.5, 0, 0, 1, 1) should be (0.5)
    linearInterpolate(0.5, 0, 1, 1, 1) should be (1)
    linearInterpolate(0.5, 0, 2, 1, 1) should be (1.5)
  }

  "linear interpolation" should "fail when not a function" in {
    evaluating(
      linearInterpolate(0.5, 1, 0, 1, 1)
    ) should produce [AssertionError]
  }

  "intersection" should "return correct value" in {
    findIntersection(0, 1, 2, 2, 5, 5, 6, 7) should be ((4, 3))
  }

  "intersection" should "return (0, 0) when parallel" in {
    findIntersection(0, 2, 2, 2, 5, 5, 6, 5) should be ((0, 0))
  }

  "intersection" should "return (-1, -1) when intersection outside of period" in {
    findIntersection(0, 1, 2, 2, 5, 7, 6, 5) should be ((-1, -1))
  }

  "intersection" should "fail when arguments not ordered" in {
    evaluating(
      findIntersection(3, 1, 2, 2, 5, 5, 6, 7)
    ) should produce [AssertionError]
  }

  "interpolation" should "fail when fewer than 4 measurements sent" in {
    val arr = Array(new TimedValue(1,1), new TimedValue(1,1), new TimedValue(1,1))

    evaluating(
      bilinear(arr)
    ) should produce [AssertionError]
  }

  "interpolation" should "be correct at intersection with 4 measurements" in {
    val arr = Array(new TimedValue(119996,1), new TimedValue(119998,2), new TimedValue(120001,5), new TimedValue(120002,7))

    bilinear(arr) should have size (1)
    bilinear(arr)(0).timestamp should be (120000)
    bilinear(arr)(0).value should be (3)
  }

  "interpolation" should "be correct left of intersection with 4 measurements" in {
    val arr = Array(new TimedValue(119997,1), new TimedValue(119999,2), new TimedValue(120002,5), new TimedValue(120003,7))

    bilinear(arr) should have size (1)
    bilinear(arr)(0).timestamp should be (120000)
    bilinear(arr)(0).value should be (2.5)
  }

  "interpolation" should "be correct right of intersection with 4 measurements" in {
    val arr = Array(new TimedValue(119995,5), new TimedValue(119997,3), new TimedValue(120001,5), new TimedValue(120002,6))

    bilinear(arr) should have size (1)
    bilinear(arr)(0).timestamp should be (120000)
    bilinear(arr)(0).value should be (4)
  }

  "interpolation" should "work with many measurements" in {
    val random : Random = new Random()

    //TODO: generate timestamps in one day range only
    val arr = new Array[TimedValue](MeasurementsPerDaySingleWire)
    for (i <- 0 until MeasurementsPerDaySingleWire)
      arr(i) = new TimedValue(random.nextLong(),random.nextDouble())

    val sorted = arr.sorted

    val start = System.currentTimeMillis
    println("returned " + bilinear(sorted).length)
    println("done in " + (System.currentTimeMillis - start))
  }

  "parallel interpolation and rollup at minute boundaries" should "work with many measurements" in {
    var msmts = List[Array[TimedValue]]()
    val random : Random = new Random()

    //TODO: generate timestamps in one day range only
    for (i <- 0 until 20) {
      val arr = new Array[TimedValue](MeasurementsPerDaySingleWire)
      for (i <- 0 until MeasurementsPerDaySingleWire)
        arr(i) = new TimedValue(random.nextLong(),random.nextDouble())

      msmts = arr.sorted :: msmts
    }

    val start = System.currentTimeMillis

    msmts map ( c => future { bilinear(c) }) map (_()) reduce (_ zip _ map (a => a._1 + a._2))

    //println("returned " + bilinear(300000, sorted).length)
    println("done in " + (System.currentTimeMillis - start))
  }

}
