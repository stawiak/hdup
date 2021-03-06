package com.os.actor

import com.os.measurement.{VampsMeasurement, CurrentMeasurement, EnergyMeasurement, Measurement}
import javax.jms._
import akka.actor._
import service.TimeWindowAware
import util.{Disconnect, ActiveMQActor}
import com.os.exchange.MeasurementXO
import com.os.exchange.json.{JSONObject, DefaultJSONFactory}
import concurrent.duration._
import akka.actor.SupervisorStrategy.{Restart, Escalate}
import scala.Some
import akka.actor.OneForOneStrategy
import javax.management.ObjectName
import com.os.util.{JMXActorBean, TimeSource, JMXNotifier}
import write.WriterMasterAware
import com.os.Settings
import java.util.UUID

/**
 * @author Vadim Bobrov
 */

/**
 * This is a simple parent just for supervisor strategy
 */
trait MessageListenerActorMBean
class MessageListenerActor(host: String, port: Int, queue: String) extends JMXNotifier with MessageListenerActorMBean with JMXActorBean {

	import context._

	var worker = watch(context.actorOf(Props(new MessageListenerChildActor(host, port, queue)), name = "worker"))

	case object RestartMessageProcessor

	var counterMsmt:Long = 0
	var counterBatch:Long = 0
	override val jmxName = new ObjectName("com.os.chaos:type=MessageListener,name=messageListener")

	override def receive: Receive = {
		// attempt to restart message processor on failure
		case Terminated(ref) =>
			system.scheduler.scheduleOnce(30 seconds, self, RestartMessageProcessor)

		case RestartMessageProcessor =>
			worker = watch(context.actorOf(Props(new MessageListenerChildActor(host, port, queue)), name = "worker"))

		case m => worker forward m
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 30 seconds) {
			case _: Exception     				=> Restart
			case _: Throwable                	=> Escalate
		}

	class MessageListenerChildActor(host: String, port: Int, queue: String) extends ActiveMQActor(host, port, queue) with TopAware with WriterMasterAware with TimeWindowAware {

		import scala.collection.JavaConversions._
		val interpolation = Settings().Interpolation
		val timeSource: TimeSource = new TimeSource {}
		val expiredTimeWindow : Duration = Settings().ExpiredTimeWindow
		var reportDisabledId: UUID = _

		override def receive: Receive = super.receive orElse { // super.receive must be enabled to process Connect and Disconnect

			case msg : MapMessage => {

				counterBatch += 1
				val customer = msg.getString("customer")
				val location = msg.getString("location")

				log.debug("picking up job for {}@{}", customer, location)

				val dataArray = DefaultJSONFactory.getInstance().jsonArrayFromZipped(msg.getString("blob"))

				for(o <-dataArray; if o.isInstanceOf[JSONObject]) {
					val mxo = new MeasurementXO()
					mxo.fromJSON(o.asInstanceOf[JSONObject])

					// Get the name if we don't have a valid mac address
					val wireid: String = if (mxo.getMac == 0L) mxo.getName else "0x" + mxo.getMac.toHexString.toUpperCase

					val jo = DefaultJSONFactory.getInstance().jsonObject(mxo.getValue)
					val value: Double = jo.optDouble("value", 0)

					val msmt: Option[Measurement] = mxo.getChannelName match {
							//TODO: remove current active energy
						case "Accumulated Energy A" | "Accumulated Energy B" | "Accumulated Energy C"            | "Current Active Energy A" | "Current Active Energy B" | "Current Active Energy C" =>
							Some(new EnergyMeasurement(customer, location, wireid, mxo.getTimestamp.getTime, value))
						case "Through RMS Current A" | "Through RMS Current B" | "Through RMS Current C" =>
							Some(new CurrentMeasurement(customer, location, wireid, mxo.getTimestamp.getTime, value))
						case "Accumulated VArs A" | "Accumulated VArs B" | "Accumulated VArs C" =>
							Some(new VampsMeasurement(customer, location, wireid, mxo.getTimestamp.getTime, value))
						case _ => None
					}

					// yes, wireid can be null!!!
					if (msmt.isDefined && wireid != null) {
						writeMaster ! msmt.get

						// if less than 9.5 minutes old - send to time window
						if (interpolation && timeSource.now - msmt.get.timestamp < expiredTimeWindow.toMillis) {
							//log.debug("sending to time window")
							timeWindow ! msmt.get
						}

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

			case Disable(id) =>
				reportDisabledId = id
				self ! Disconnect

				become(collecting)
				// send marker to be added after all other messages have been processed
				self ! Done


		}

		def collecting(): Receive = {
			case Done =>
				become(FSM.NullFunction)
				sender ! Disabled(reportDisabledId)
		}

	}

}
