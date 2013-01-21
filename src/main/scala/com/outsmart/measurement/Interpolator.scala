package com.outsmart.measurement

import scala._


/**
 * given a number of measurements return interpolated values
 * at minute boundaries using bilinear interpolation
 *
 * prereq:  all timestamps are unique,
 *          4 or more measurements
 *          sorted by time
 *
 * @author Vadim Bobrov
 */
class Interpolator(val src : Iterator[TimedValue], val boundary: Int = 60000) extends Iterable[TimedValue] {

	import Interpolator._

	class InterpolatedIterator(val source : Iterator[TimedValue]) extends Iterator[TimedValue] {

		var tv1, tv2, tv3, tv4 : Option[TimedValue] = None
		var cur : Iterator[TimedValue] = Iterator.empty

		def hasNext: Boolean = {
			// we still have some values from previous 4 points or
			if (cur.hasNext)
				true
			else {
				// we need to move to next 4 measurements and try generating interpolations
				while(source.hasNext) {
					tv1 = tv2; tv2 = tv3; tv3 = tv4; tv4 = Option(source.next())

					// all 4 points filled?
					if (tv1 != None && tv2 != None && tv3 != None && tv4 != None) {
						cur = bilinear(tv1.get, tv2.get, tv3.get, tv4.get, boundary).iterator
						if (cur.hasNext)
							return true
					}
				}

				// no interpolations found and source exhausted
				false
			}

		}

		def next(): TimedValue = cur.next()

	}

	def iterator: Iterator[TimedValue] = new InterpolatedIterator(src)


}

object Interpolator {
	/**
	 * Calculates interpolation given four consecutive points
	 * including second and third point if fall on the boundary
	 *
	 * @param tv1 tv2 tv3 tv4	four consecutive points (must be in ascending order)
	 * @return					sequence of interpolations
	 */
	def bilinear(tv1 : TimedValue, tv2 : TimedValue, tv3 : TimedValue, tv4 : TimedValue, boundary: Int = 60000) : Seq[TimedValue] = {
		// ensure strictly ascending
		//require(tv1 < tv2 && tv2 < tv3 && tv3 < tv4, "bilinear arguments out of order " + tv1.timestamp + " " + tv3.timestamp + " " + tv3.timestamp + " " + tv4.timestamp)

		var output = List[TimedValue]()

		//if(!(tv1 < tv2 && tv2 < tv3 && tv3 < tv4)) {
		if(!(tv1.compareTo(tv2) < 0 && tv2.compareTo(tv3) < 0 && tv3.compareTo(tv4) < 0 )) {
			//debug("skipping duplicate")
			return output
		}

		if (tv2.timestamp % boundary == 0)
			output = tv2 :: output


		// take 4 points and find intersection
		val (x, y) = findIntersection(
			tv1.timestamp, tv1.value,
			tv2.timestamp, tv2.value,
			tv3.timestamp, tv3.value,
			tv4.timestamp, tv4.value
		)

		for (innerBoundary <- tv2.timestamp - (tv2.timestamp % boundary) + boundary until tv3.timestamp by boundary) {

			var computedValue: Double = 0

			if (x > 0) {
				// intersection found
				// compare current minute boundary with intersection
				// compute interpolation using ether lower or upper 2 points
				if (x == innerBoundary)
					computedValue = y
				else if (x < innerBoundary)
					computedValue = linearInterpolate(innerBoundary, x, y, tv3.timestamp, tv3.value)
				else
					computedValue = linearInterpolate(innerBoundary, tv2.timestamp, tv2.value, x, y)

			} else {
				// no intersection - either parallel or outside period
				//use linear interpolation between two measurements
				computedValue = linearInterpolate(innerBoundary, tv2.timestamp, tv2.value, tv3.timestamp, tv3.value)
			}

			// add to output
			output = new TimedValue(innerBoundary, computedValue) :: output
		}

		if (tv3.timestamp % boundary == 0)
			output = tv3 :: output

		output
	}

	/**
	 * Find the X point of intersection of two lines
	 * even though X axis is longs (timestamps) they are converted to doubles to avoid division error
	 * the output is rounded to long again
	 *
	 * params: xy12 - 2 points of the first line, xy34 - 2 points of the second line
	 *
	 * prereq: strictly in ascending order by X
	 * @return (0,0) if parallel, (-1,-1) if intersect outside [x2, x3], coordinates of intersection otherwise
	 */
	def findIntersection(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double, x4: Double, y4: Double): (Long, Double) = {
		// ensure strictly ascending
		require(x1 < x2 && x2 < x3 && x3 < x4, "findIntersection arguments out of order")

		val slope1 = (y2 - y1)/(x2 - x1)
		val slope2 = (y4 - y3)/(x4 - x3)

		// if parallel
		if (slope1 == slope2)
			return (0, 0)

		val x = math.round((slope1 * x1 - y1 - slope2 * x3 + y3) / (slope1 - slope2))
		val y = (slope1 * slope2 * (x1 - x3) + slope1 * y3 - slope2 * y1) / (slope1 - slope2)

		if (x >= x2 && x <= x3) (x, y) else (-1, -1)
	}

	/**
	 * Linear interpolation (or extrapolation) at point x on a line with x1y1, x2y2
	 * @param x interpolation point converted to double for math
	 * @return interpolation value
	 */
	def linearInterpolate(x: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
		require(x1 != x2, "linearInterpolate arguments out of order")
		(x - x1) * (y2 - y1)/(x2 - x1) + y1
	}

}