package com.os.actor.read

import akka.actor.{ActorLogging, Actor}
import com.os.dao.Scanner
import com.os.Settings

/**
  * @author Vadim Bobrov
  */
class ReadWorkerActor(val tableName : String) extends Actor with ActorLogging{

	val scanner = Scanner(tableName, Settings(context.system.settings.config))


	override def receive: Receive = {

		case request : MeasurementReadRequest => {
			log.debug("scan started for {} in {}", request, this.hashCode())
			sender ! scanner.scan(request.customer, request.location, request.wireid, request.period)
		}

		case request : RollupReadRequest => {
			log.debug("scan started for {} in {}", request, this.hashCode())
			sender ! scanner.scan(request.customer, request.location, request.period)
		}

	}

 }
