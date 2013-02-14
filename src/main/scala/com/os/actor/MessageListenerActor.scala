package com.os.actor

import com.os.measurement.{VampsMeasurement, CurrentMeasurement, EnergyMeasurement, Measurement}
import javax.jms._
import akka.actor.PoisonPill
import util.{GracefulStop, ActiveMQActor}
import com.os.exchange.MeasurementXO
import com.os.exchange.json.{JSONObject, DefaultJSONFactory}

/**
 * @author Vadim Bobrov
 */
class MessageListenerActor(host: String, port: Int, queue: String) extends ActiveMQActor(host, port, queue) with TopAware {

	import scala.collection.JavaConversions._
	val timeWindow = context.system.actorFor("/user/top/timeWindow")

	override def receive: Receive = {

		case msg : MapMessage => {

			val customer = msg.getString("customer")
			val location = msg.getString("location")

			val dataArray = DefaultJSONFactory.getInstance().jsonArrayFromZipped(msg.getString("blob"))

			for(o <-dataArray; if o.isInstanceOf[JSONObject]) {
				val mxo = new MeasurementXO()
				mxo.fromJSON(o.asInstanceOf[JSONObject])

				// Get the name if we don't have a valid mac address
				val wireid: String = if (mxo.getMac == 0L) mxo.getName else "0x" + mxo.getMac.toHexString.toUpperCase


				val jo = DefaultJSONFactory.getInstance().jsonObject(mxo.getValue)
				val value: Double = jo.optDouble("value", 0)

				val msmt: Option[Measurement] = mxo.getChannelName match {
					case "Accumulated Energy A" | "Accumulated Energy B" | "Accumulated Energy C" =>
						Some(new EnergyMeasurement(customer, location, wireid, mxo.getTimestamp.getTime, value))
					case "Through RMS Current A" | "Through RMS Current B" | "Through RMS Current C" =>
						Some(new CurrentMeasurement(customer, location, wireid, mxo.getTimestamp.getTime, value))
					case "Accumulated VArs A" | "Accumulated VArs B" | "Accumulated VArs C" =>
						Some(new VampsMeasurement(customer, location, wireid, mxo.getTimestamp.getTime, value))
					case _ => None
				}

				if (msmt.isDefined)
					timeWindow ! msmt.get
			}

		}

		case msg : TextMessage => {
			log.debug("received text message {}", msg.getText)
			if (msg.getText.equalsIgnoreCase("stop"))
				top ! GracefulStop
		}

		case msg : Message => {
			log.debug("ignored message {}", msg)
		}

		case GracefulStop =>
			self ! PoisonPill

	}

}
