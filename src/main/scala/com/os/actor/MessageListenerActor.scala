package com.os.actor

import com.os.measurement.{VampsMeasurement, CurrentMeasurement, EnergyMeasurement, Measurement}
import javax.jms._
import akka.actor._
import util.{FinalCountDown, GracefulStop, ActiveMQActor}
import com.os.exchange.MeasurementXO
import com.os.exchange.json.{JSONObject, DefaultJSONFactory}
import concurrent.duration._
import akka.actor.SupervisorStrategy.{Restart, Escalate}
import scala.Some
import akka.actor.OneForOneStrategy

/**
 * @author Vadim Bobrov
 */

/**
 * This is a simple parent just for supervisor strategy
 */
class MessageListenerActor(host: String, port: Int, queue: String) extends FinalCountDown {

	import context._

	var worker = watch(context.actorOf(Props(new MessageListenerChildActor(host, port, queue)), name = "messageProcessor"))

	case object RestartMessageProcessor

	var counterMsmt:Long = 0
	var counterBatch:Long = 0

	override def receive: Receive = {
		case Terminated(ref) =>
			system.scheduler.scheduleOnce(30 seconds, self, RestartMessageProcessor)

		case RestartMessageProcessor =>
			worker = watch(context.actorOf(Props(new MessageListenerChildActor(host, port, queue)), name = "messageProcessor"))

		case GracefulStop =>
			waitAndDie()
			children foreach  (_ ! GracefulStop)

		case m => worker forward m
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 30 seconds) {
			case _: Exception     				=> Restart
			case _: Throwable                	=> Escalate
		}

	class MessageListenerChildActor(host: String, port: Int, queue: String) extends ActiveMQActor(host, port, queue) with TopAware {

		import scala.collection.JavaConversions._
		val timeWindow = context.system.actorFor("/user/top/timeWindow")

		override def receive: Receive = super.receive orElse { // super.receive must be enabled to process Connect

			case msg : MapMessage => {

				counterBatch += 1
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

					if (msmt.isDefined) {
						timeWindow ! msmt.get
						counterMsmt += 1
					}
				}

			}

			case msg : TextMessage =>
				log.debug("received text message {}", msg.getText)
				if (msg.getText.equalsIgnoreCase("stop"))
					top ! GracefulStop

			case msg : Message =>
				log.debug("ignored message {}", msg)

			case Monitor =>
				sender ! Map("msmt" -> counterMsmt, "batch" -> counterBatch)

			case GracefulStop =>
				self ! PoisonPill

		}

	}

}
