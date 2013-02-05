package com.os.measurement


/**
 * @author Vadim Bobrov
 */
trait Interpolated
trait Rollup
class Measurement(
					 val customer: String,
					 val location: String,
					 val wireid: String,
					 override val timestamp: Long,
					 override val energy: Double,
					 override val current: Double,
					 override val vampire: Double
					 )

	extends MeasuredValue(timestamp, energy, current, vampire){

	override def toString = "measurement " + customer + "/" + location + "/" + wireid + " " + super.toString

	override def equals(other : Any)  = other match {
    case that: Measurement =>
      that.customer == this.customer &&
      that.location == this.location &&
      that.wireid == this.wireid &&
      that.timestamp == this.timestamp &&
      that.energy == this.energy &&
      that.current == this.current &&
      that.vampire == this.vampire
    case _ => false
	}

  //TODO: hashCode!!!
}
