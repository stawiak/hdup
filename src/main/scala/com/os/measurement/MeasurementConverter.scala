package com.os.measurement

import javax.jms.{TextMessage, Session, MapMessage}
import com.os.util.Timing

/**
 * @author Vadim Bobrov
 */
object MeasurementConverter extends Timing {

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

	// to and from JMS TextMessage
	implicit def measurementToJMSTextMessage(msmt: Measurement)(implicit session: Session): TextMessage = {
		val sep = '?'
		val sb = new StringBuilder()

		sb.append(msmt.customer)
		sb.append(sep)
		sb.append(msmt.location)
		sb.append(sep)
		sb.append(msmt.wireid)
		sb.append(sep)
		sb.append(msmt.timestamp)
		sb.append(sep)
		sb.append(msmt.energy)
		sb.append(sep)
		sb.append(msmt.current)
		sb.append(sep)
		sb.append(msmt.vampire)

		session.createTextMessage(sb.result())
	}

	implicit def jmsTextMessageToMeasurement(msg: TextMessage): Measurement = {
		new Measurement("","","", 1,1,1,1)
	}

}
