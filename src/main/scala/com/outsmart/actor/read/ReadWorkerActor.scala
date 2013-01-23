package com.outsmart.actor.read

import akka.actor.{ActorLogging, Actor}
import com.outsmart.dao.{Scanner, Writer}
import org.joda.time.DateTime

/**
  * @author Vadim Bobrov
  */
class ReadWorkerActor(val tableName : String) extends Actor with ActorLogging{

	val writer = Writer(tableName)


	override def receive: Receive = {

		case request : MeasurementScanRequest => {
			sender ! Scanner(tableName).scan(request.customer, request.location, request.wireid, new DateTime(request.period._1), new DateTime(request.period._2))
		}

		case request : RollupScanRequest => {
			sender ! Scanner(tableName).scan(request.customer, request.location, new DateTime(request.period._1), new DateTime(request.period._2))
		}

	}

 }
