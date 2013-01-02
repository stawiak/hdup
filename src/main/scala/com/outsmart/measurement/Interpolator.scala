package com.outsmart.measurement

/**
 * @author Vadim Bobrov
 */
object Interpolator {

	/**
	 * given a number of measurements return interpolated values
	 * at minute boundaries using bilinear interpolation
	 *
	 * prereq:  all timestamps are unique,
	 *          4 or more measurements
	 *          sorted by time
	 * @param arg       actual measurements
	 * @param boundary  time boundary, one minute by default
	 * @return    interpolated values at minute boundaries
	 */
	def bilinear(arg: Array[TimedValue], boundary: Int = 60000) : Seq[TimedValue] = {
		assert(arg.length >= 4)

		var output = List[TimedValue]()
		//TODO; no minute boundaries inside 2 first and 2 last values

		var currentMinBoundary = arg(1).timestamp + boundary - (arg(1).timestamp % boundary)

		//TODO: use sliding window
		for (i <- 2 until arg.length - 1) {
			// direct hit
			if (arg(i).timestamp == currentMinBoundary) {
				// add to output
				output = new TimedValue(currentMinBoundary, arg(i).value) :: output

				// set next minute boundary
				currentMinBoundary += boundary
			}

			// we crossed current and possibly more minute boundaries
			// we could also land on one of the minute boundaries

			// if there is a minute boundary between current and next measurement
			// calculate interpolated value for it and set next minute boundary
			if (arg(i).timestamp > currentMinBoundary) {

				val upper = arg(i).timestamp - (arg(i).timestamp % boundary)

				// for every minute boundary crossed
				for (crossedBoundary <- currentMinBoundary to upper by boundary) {
					// take 4 points and find intersection
					val (x, y) = findIntersection(
						arg(i - 2).timestamp, arg(i - 2).value,
						arg(i - 1).timestamp, arg(i - 1).value,
						arg(i).timestamp, arg(i).value,
						arg(i + 1).timestamp, arg(i + 1).value
					)

					var computedValue: Double = 0

					if (x > 0) {
						// intersection found
						// compare current minute boundary with intersection
						// compute interpolation using ether lower or upper 2 points
						if (x == crossedBoundary)
							computedValue = y
						else if (x < crossedBoundary)
							computedValue = linearInterpolate(crossedBoundary, x, y, arg(i).timestamp, arg(i).value)
						else
							computedValue = linearInterpolate(crossedBoundary, arg(i - 1).timestamp, arg(i - 1).value, x, y)

					} else {
						// no intersection - either parallel or outside period
						//use linear interpolation between two measurements
						computedValue = linearInterpolate(crossedBoundary, arg(i - 1).timestamp, arg(i - 1).value, arg(i).timestamp, arg(i).value)
					}

					// add to output
					output = new TimedValue(crossedBoundary, computedValue) :: output
				}

				currentMinBoundary = upper + boundary
			}
		}

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
		assert(x1 < x2 && x2 < x3 && x3 < x4)

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
		assert(x1 != x2)
		(x - x1) * (y2 - y1)/(x2 - x1) + y1
	}
}
