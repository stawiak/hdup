package com.os.measurement

import com.os.dao.Saveable


/**
 * @author Vadim Bobrov
 */
trait Interpolated
trait Rollup
sealed abstract class Measurement(val customer: String, val location: String, val wireid: String, val timestamp: Long, val value: Double) extends Ordered[Measurement] with Saveable {

	override def toString = "msmt " + customer + "/" + location + "/" + wireid + "/" + timestamp + "/" + value

	override def compare(that : Measurement) : Int = {
		val timestampRes = this.timestamp.compareTo(that.timestamp)
		if (timestampRes != 0) return timestampRes

		val customerRes = this.customer.compareTo(that.customer)
		if (customerRes != 0) return customerRes

		val locationRes = this.location.compareTo(that.location)
		if (locationRes != 0) return locationRes

		val wireidRes = this.wireid.compareTo(that.wireid)
		if (wireidRes != 0) return wireidRes

		0
	}

	override def equals(other : Any)  = other match {
		case that: Measurement =>
        (that canEqual this) &&
			  that.customer == this.customer &&
				that.location == this.location &&
				that.wireid == this.wireid &&
				that.timestamp == this.timestamp
		case _ => false
	}

	override def hashCode(): Int = {
		var result = customer.hashCode()
		result = 31 * result + location.hashCode()
		result = 31 * result + wireid.hashCode()
		result = 31 * result + (timestamp ^ (timestamp >>> 32)).asInstanceOf[Int]
		result
	}

  def canEqual(other: Any) = other.isInstanceOf[Measurement]
}

class EnergyMeasurement(override val customer: String, override val location: String, override val wireid: String, override val timestamp: Long, override val value: Double) extends Measurement(customer, location, wireid, timestamp, value) {
  	override def canEqual(other: Any) = other.isInstanceOf[EnergyMeasurement]
	override def toString = "energy " + customer + "/" + location + "/" + wireid + "/" + timestamp + "/" + value
}
class CurrentMeasurement(override val customer: String, override val location: String, override val wireid: String, override val timestamp: Long, override val value: Double) extends Measurement(customer, location, wireid, timestamp, value) {
  	override def canEqual(other: Any) = other.isInstanceOf[CurrentMeasurement]
	override def toString = "current " + customer + "/" + location + "/" + wireid + "/" + timestamp + "/" + value
}
class VampsMeasurement(override val customer: String, override val location: String, override val wireid: String, override val timestamp: Long, override val value: Double) extends Measurement(customer, location, wireid, timestamp, value) {
  	override def canEqual(other: Any) = other.isInstanceOf[VampsMeasurement]
	override def toString = "vamps " + customer + "/" + location + "/" + wireid + "/" + timestamp + "/" + value
}
