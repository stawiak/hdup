package com.outsmart.measurement

/**
 * @author Vadim Bobrov
 */
class Measurement(val customer: String, val location: String, val wireid: String, override val timestamp: Long, override val value: Long) extends MeasuredValue(timestamp, value){
  override def toString = customer + "/" + location + "/" + wireid + " ts: " + timestamp + " value: " + value
}
