package com.outsmart.measurement

/**
 * @author Vadim Bobrov
*/
class MeasuredValue(val timestamp : Long, val energy : Double, val current: Double, val vampire : Double, val interpolated : Boolean = false) extends Ordered[MeasuredValue] {
  	override def toString = "ts: " + timestamp + " energy: " + energy + " current: " + current + " vampire: " + vampire
  	override def compare(that : MeasuredValue) : Int = this.timestamp.compareTo(that.timestamp)

}

object MeasuredValue {
  	implicit def longToMeasuredValue(x: Double) = new MeasuredValue(System.currentTimeMillis, x, x, x)

	/**
	 * given a number of measurements find the closest exact
	 * minute boundary below and over
	 * @param arg measurements
	 * @return    tuple of longs representing time in milliseconds since epoch
	 */
	def minuteBoundary(arg: Seq[MeasuredValue]) : (Long, Long) = {
		// don't need to specify ordering method anymore
		//val sorted = arg sortWith (_.timestamp < _.timestamp)
		val sorted = arg.sorted
		val first = (sorted head).timestamp
		val last = (sorted last).timestamp
		(first - (first % 60000), last + 60000 - (last % 60000))
	}

	/**
	 * given a number of measurements find the exact minute
	 * point approximations
	 * @param arg measurements
	 * @return    array of approximations from lower to higher point in the original array
	 */
	def minuteApprx(arg: Seq[MeasuredValue]) : Seq[MeasuredValue] = {
		val borders = minuteBoundary(arg)

		//TODO: !!!!!!!!!
		for (ts <- borders._1 to borders._2 by 60000)
		yield null //new MeasuredValue(ts, apprx(ts, arg))
	}


	def apprx(ts:Long, arg: Seq[MeasuredValue]) : Long = {
		return 0; //TODO: !!!!!!!!!
	}
}
