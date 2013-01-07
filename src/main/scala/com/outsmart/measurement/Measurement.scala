package com.outsmart.measurement

/**
 * @author Vadim Bobrov
 */
class Measurement(
					 val customer: String,
					 val location: String,
					 val wireid: String,
					 override val timestamp: Long,
					 override val energy: Double,
					 override val current: Double,
					 override val vampire: Double,
					 override val tags : Option[Tag] = None
					 )

	extends MeasuredValue(timestamp, energy, current, vampire){
  override def toString = customer + "/" + location + "/" + wireid + " " + super.toString
}
