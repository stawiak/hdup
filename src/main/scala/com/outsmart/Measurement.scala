package com.outsmart

/**
 * @author Vadim Bobrov
*/
class Measurement(val value : Long, val timestamp : Long) extends Ordered[Measurement] {
  override def toString = "ts: " + timestamp + " value: " + value
  override def compare(that : Measurement) : Int = {
    if(this.timestamp - that.timestamp == 0) 0
    else if ((this.timestamp - that.timestamp < 0)) -1
    else 1
  }
}

object Measurement {
  implicit def longToMeasurement(x: Long) = new Measurement(x, System.currentTimeMillis)


  /**
   * given a number of measurements find the closest exact
   * minute boundary below and over
   * @param arg measurements
   * @return    tuple of longs representing time in milliseconds since epoch
   */
  def minuteBoundary(arg: Seq[Measurement]) : (Long, Long) = {
    // don't need to specify ordering method anymore
    val sorted = arg sortWith (_.timestamp < _.timestamp)
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
  def minuteApprx(arg: Seq[Measurement]) : Seq[Measurement] = {
    val borders = minuteBoundary(arg)

    for (ts <- borders._1 to borders._2 by 60000)
    yield new Measurement(apprx(ts, arg), ts)
  }


  def apprx(ts:Long, arg: Seq[Measurement]) : Long = {
    return 0; //TODO: !!!!!!!!!
  }
}
