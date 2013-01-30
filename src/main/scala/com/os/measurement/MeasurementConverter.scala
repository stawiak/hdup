package com.os.measurement

import javax.jms.{Session, MapMessage}

/**
 * @author Vadim Bobrov
 */
object MeasurementConverter {

	// to and from JMS MapMessage
	implicit def measurementToJMSMapMessage(msmt: Measurement)(implicit session: Session): MapMessage = {
		val msg = session.createMapMessage()
		msg.setString("customer", msmt.customer)
		msg.setString("location", msmt.location)
		msg.setString("wireid", msmt.wireid)

		msg.setLong("timestamp", msmt.timestamp)
		msg.setDouble("energy", msmt.energy)
		msg.setDouble("current", msmt.current)
		msg.setDouble("vampire", msmt.vampire)
		msg
	}

	implicit def jmsMapMessageToMeasurement(msg: MapMessage): Measurement = {
		new Measurement(
			msg.getString("customer"),
			msg.getString("location"),
			msg.getString("wireid"),

			msg.getLong("timestamp"),
			msg.getDouble("energy"),
			msg.getDouble("current"),
			msg.getDouble("vampire")
		)
	}

}
