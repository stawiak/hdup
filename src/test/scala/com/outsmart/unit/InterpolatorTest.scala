package com.outsmart.unit

import org.scalatest.{FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.outsmart.measurement.{TimedValue, Interpolator}
import util.Random
import actors.Futures._
import collection.SortedSet

/**
 * @author Vadim Bobrov
 */
class InterpolatorTest extends FlatSpec with ShouldMatchers {

	import Interpolator._

	val MeasurementsPerDaySingleWire = 140040
	val MillisPerDay = 86400000

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

	"interpolation" should "be correct when intersection is on a lower measurement with 4 measurements" in {
		val arr = Array(new TimedValue(119997,1), new TimedValue(119999,2), new TimedValue(120001,6), new TimedValue(120002,8))

		bilinear(arr) should have size (1)
		bilinear(arr)(0).timestamp should be (120000)
		bilinear(arr)(0).value should be (4)
	}

	"interpolation" should "be correct when intersection is on a upper measurement with 4 measurements" in {
		val arr = Array(new TimedValue(119996,1), new TimedValue(119998,2), new TimedValue(120002,4), new TimedValue(120003,6))

		bilinear(arr) should have size (1)
		bilinear(arr)(0).timestamp should be (120000)
		bilinear(arr)(0).value should be (3)
	}

	"interpolation" should "be correct with 2 or more minute boundaries between measurements" in {
		val arr = Array(new TimedValue(119995,5), new TimedValue(119997,3), new TimedValue(180001,60005), new TimedValue(180002,60006))

		bilinear(arr) should have size (2)

		bilinear(arr)(1).timestamp should be (120000)
		bilinear(arr)(1).value should be (4)

		bilinear(arr)(0).timestamp should be (180000)
		bilinear(arr)(0).value should be (60004)
	}

	"interpolation" should "work with one device measurements within one day" in {
		val random : Random = new Random()

		var uniqueValues = SortedSet[TimedValue]()

		//val arr = new Array[TimedValue](MeasurementsPerDaySingleWire)

		for (i <- 0 until MeasurementsPerDaySingleWire)
			uniqueValues += new TimedValue(random.nextInt(MillisPerDay),random.nextDouble())

		val sorted = uniqueValues.toArray
		println("length after " + sorted.length)

		val start = System.currentTimeMillis
		println("returned " + bilinear(sorted).length)
		println("done in " + (System.currentTimeMillis - start))
	}

	"interpolation" should "work with multiple device measurements within one day" in {
		val random : Random = new Random()
		var totalTime = 0L

		for (i <- 1 to 100) {
			var uniqueValues = SortedSet[TimedValue]()

			for (i <- 0 until MeasurementsPerDaySingleWire)
				uniqueValues += new TimedValue(random.nextInt(MillisPerDay),random.nextDouble())

			val sorted = uniqueValues.toArray

			val start = System.currentTimeMillis
			bilinear(sorted)
			totalTime += (System.currentTimeMillis - start)
		}

		println("total time " + totalTime)

	}

	"parallel interpolation and rollup at minute boundaries" should "work with many measurements" in {
		var msmts = List[Array[TimedValue]]()
		val random : Random = new Random()

		for (i <- 0 until 40) {
			var uniqueValues = SortedSet[TimedValue]()
			for (i <- 0 until MeasurementsPerDaySingleWire)
				uniqueValues += new TimedValue(random.nextInt(MillisPerDay),random.nextDouble())

			msmts = uniqueValues.toArray :: msmts
		}

		val start = System.currentTimeMillis

		msmts map ( c => future { bilinear(c) }) map (_()) reduce (_ zip _ map (a => a._1 + a._2))

		println("done in " + (System.currentTimeMillis - start))
	}


}
