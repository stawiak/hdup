package com.os.actor.read

import akka.actor.{PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParsers
import com.os.actor.util.GracefulStop
import com.os.mql.model.MQLExecutor
import com.os.rest.exchange.TimeSeriesData
import concurrent.Await
import com.os.measurement.TimedValue
import java.sql.Timestamp
import akka.util.Timeout
import akka.pattern.ask
import concurrent.duration._

/**
  * @author Vadim Bobrov
  */
class MQLHandlerActor extends Actor with ActorLogging with ReadMasterAware {

	implicit val timeout: Timeout = 60 seconds
	//TODO: singleton or multiple?
	val parser = new MQLParsers()


	override def receive: Receive = {

		case mql: String =>
			//TODO: error handling
			//TODO: where is condition like value > _ handled?
			log.debug("mql received {}", mql)
			val query = parser.parseAll(parser.mql, mql)
			val executor = new MQLExecutor(query.get)

			val tsd = new TimeSeriesData()

			executor.generate foreach (readRequest => {
				try {
					Await.result((readMaster ? readRequest).mapTo[Iterable[TimedValue]], timeout.duration) foreach (mv => tsd.put(new Timestamp(mv.timestamp), mv.value))
				} catch {
					case e: Exception =>
						log.error(e, e.getMessage)
				}
			})

			sender ! tsd.toJSONString


		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}


 }
