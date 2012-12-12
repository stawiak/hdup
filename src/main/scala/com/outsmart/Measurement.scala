package com.outsmart

/**
 * @author Vadim Bobrov
*/
class Measurement(val value : Long, val timestamp : Long) {
  override def toString = "ts: " + timestamp + " value: " + value
}

object Measurement {
  implicit def longToMeasurement(x: Long) = new Measurement(x, System.currentTimeMillis)
}
