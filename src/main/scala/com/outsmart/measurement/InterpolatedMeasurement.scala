package com.outsmart.measurement

/**
 * Wrapper tagging measurement as interpolated
 * @author Vadim Bobrov
 */
class InterpolatedMeasurement(
					 override val customer: String,
					 override val location: String,
					 override val wireid: String,
					 override val timestamp: Long,
					 override val energy: Double,
					 override val current: Double,
					 override val vampire: Double,
					 override val tags : Option[Tag] = None
					 )

	extends Measurement(customer, location, wireid, timestamp, energy, current, vampire)  {}

