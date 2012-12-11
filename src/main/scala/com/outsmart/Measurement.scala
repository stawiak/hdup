package com.outsmart

/**
 * @author Vadim Bobrov
*/
class Measurement(v : Long, ts : Long) {

  val value: Long = v
  val timestamp: Long = ts

  override def toString = "ts: " + timestamp + " value: " + value
}
