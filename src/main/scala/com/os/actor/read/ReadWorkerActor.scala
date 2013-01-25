package com.os.actor.read

import akka.actor.{ActorLogging, Actor}
import com.os.dao.{Scanner, Writer}
import org.joda.time.DateTime

/**
  * @author Vadim Bobrov
  */
class ReadWorkerActor(val tableName : String) extends Actor with ActorLogging{

	val scanner = Scanner(tableName)


	override def receive: Receive = {

		case request : MeasurementScanRequest => {
			sender ! scanner.scan(request.customer, request.location, request.wireid, request.period)
		}

		case request : RollupScanRequest => {
			sender ! scanner.scan(request.customer, request.location, request.period)
		}

	}

 }
