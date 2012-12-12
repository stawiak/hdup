package com.outsmart

/**
 * @author Vadim Bobrov
*/
class Measurement(val value : Long, val timestamp : Long) {
  override def toString = "ts: " + timestamp + " value: " + value
}

object Measurement {
  implicit def longToMeasurement(x: Long) = new Measurement(x, System.currentTimeMillis)


  /**
   * given a number of measurements find the exact minute
   * boundary below and over
   * @param arg measurements
   * @return    tuple of longs representing time in milliseconds since epoch
   */
  def minuteBoundary(arg: Array[Measurement]) : (Long, Long) = {
    val sorted = arg sortWith (_.timestamp < _.timestamp)
    val first = sorted(0).timestamp
    val last = sorted(sorted.length - 1).timestamp
    (first - (first % 60000), last + 60000 - (last % 60000))
  }


}
