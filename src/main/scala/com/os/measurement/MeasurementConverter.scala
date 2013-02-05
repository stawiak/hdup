package com.os.measurement

import javax.jms.{TextMessage, Session, MapMessage}
import com.os.util.Timing

/**
 * @author Vadim Bobrov
 */
object MeasurementConverter extends Timing {

	val sep = '?'

	// to and from JMS MapMessage
	implicit def measurementToJMSMapMessage(msmt: Measurement)(implicit session: Session): MapMessage = {
		val msg = session.createMapMessage()
		msg.setString("customer", msmt.customer)
		msg.setString("location", msmt.location)
		msg.setString("wireid", msmt.wireid)

		msg.setLong("timestamp", msmt.timestamp)
		msg.setDouble("value", msmt.value)
		msg
	}

	implicit def jmsMapMessageToMeasurement(msg: MapMessage): Option[Measurement] = {
		try {
			//TODO
			Some(new EnergyMeasurement(
				msg.getString("customer"),
				msg.getString("location"),
				msg.getString("wireid"),

				msg.getLong("timestamp"),
				msg.getDouble("value")
			))
		} catch {
			case e: Exception =>
				log.error(e.getMessage, e)
				None
		}
	}

	// to and from JMS TextMessage
	implicit def measurementToJMSTextMessage(msmt: Measurement)(implicit session: Session): TextMessage = {

		val sb = new StringBuilder()

		sb.append(msmt.customer)
		sb.append(sep)
		sb.append(msmt.location)
		sb.append(sep)
		sb.append(msmt.wireid)
		sb.append(sep)
		sb.append(msmt.timestamp)
		sb.append(sep)
		sb.append(msmt.value)

		session.createTextMessage(sb.result())
	}

	implicit def jmsTextMessageToMeasurement(msg: TextMessage): Option[Measurement] = {
		val values = msg.getText.split(sep)

		try {
			//TODO
			Some(new EnergyMeasurement(values(0),values(1),values(2), values(3).toLong, values(4).toDouble))
		} catch {
			case e: Exception =>
				log.error(e.getMessage, e)
				None
		}
	}

}
