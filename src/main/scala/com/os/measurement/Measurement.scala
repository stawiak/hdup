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

	override def equals(that : Any)  = {
		that.isInstanceOf[Measurement] &&
		that.asInstanceOf[Measurement].customer == this.customer &&
		that.asInstanceOf[Measurement].location == this.location &&
		that.asInstanceOf[Measurement].wireid == this.wireid &&
		that.asInstanceOf[Measurement].timestamp == this.timestamp &&
		that.asInstanceOf[Measurement].energy == this.energy &&
		that.asInstanceOf[Measurement].current == this.current &&
		that.asInstanceOf[Measurement].vampire == this.vampire
	}

}
