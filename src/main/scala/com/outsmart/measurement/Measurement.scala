package com.outsmart.measurement

/**
 * @author Vadim Bobrov
 */
class Measurement(val customer: String, val location: String, val wireid: String, override val value: Long, override val timestamp: Long) extends MeasuredValue(value, timestamp){
  override def toString = customer + "/" + location + "/" + wireid + " ts: " + timestamp + " value: " + value
}
