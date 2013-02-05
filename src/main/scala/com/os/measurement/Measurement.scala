package com.os.measurement


/**
 * @author Vadim Bobrov
 */
trait Interpolated
trait Rollup
sealed abstract class Measurement(val customer: String, val location: String, val wireid: String, val timestamp: Long, val value: Double) extends Ordered[Measurement] {

	override def toString = "msmt " + customer + "/" + location + "/" + wireid + "/" + timestamp + "/" + value

	override def compare(that : Measurement) : Int = this.timestamp.compareTo(that.timestamp)

}

case class EnergyMeasurement(override val customer: String, override val location: String, override val wireid: String, override val timestamp: Long, override val value: Double) extends Measurement(customer, location, wireid, timestamp, value)
case class CurrentMeasurement(override val customer: String, override val location: String, override val wireid: String, override val timestamp: Long, override val value: Double) extends Measurement(customer, location, wireid, timestamp, value)
case class VampsMeasurement(override val customer: String, override val location: String, override val wireid: String, override val timestamp: Long, override val value: Double) extends Measurement(customer, location, wireid, timestamp, value)
