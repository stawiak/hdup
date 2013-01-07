package com.outsmart.measurement

/**
 * @author Vadim Bobrov
*/
class TimedValue(val timestamp : Long, val value : Double) extends Ordered[TimedValue] {
  	override def toString = "ts: " + timestamp + " value: " + value
  	override def compare(that : TimedValue) : Int = this.timestamp.compareTo(that.timestamp)

  	override def equals(that : Any)  = {
		that.isInstanceOf[TimedValue] && that.asInstanceOf[TimedValue].timestamp == this.timestamp
  	}

  	// define addition as adding values
  	def +(that: TimedValue): TimedValue = new TimedValue(timestamp, value + that.value)

	// by default use energy as value
	implicit def measuredValueToTimedValue(x: MeasuredValue) = new TimedValue(x.timestamp, x.energy)
}


