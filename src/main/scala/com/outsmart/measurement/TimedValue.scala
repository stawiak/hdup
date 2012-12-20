package com.outsmart.measurement

/**
 * @author Vadim Bobrov
*/
class TimedValue(val timestamp : Long, val value : Double) extends Ordered[TimedValue] {
  override def toString = "ts: " + timestamp + " value: " + value
  override def compare(that : TimedValue) : Int = {
    if(this.timestamp - that.timestamp == 0) 0
    else if ((this.timestamp - that.timestamp < 0)) -1
    else 1
  }
}


