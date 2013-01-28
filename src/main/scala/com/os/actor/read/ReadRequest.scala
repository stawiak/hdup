package com.os.actor.read

import org.joda.time.Interval

/**
 * @author Vadim Bobrov
 */
case class MeasurementScanRequest(customer : String, location : String, wireid: String, period : Interval)
case class RollupScanRequest(customer : String, location : String, period : Interval)

case class MeasurementReadRequest(customer : String, location : String, wireid: String, periods : Array[Interval]) {

	def scanRequests() = {
		(periods map (MeasurementScanRequest(customer, location, wireid, _))).toTraversable
	}

}

case class RollupReadRequest(customer : String, location : String, periods : Array[Interval]) {

	def scanRequests() = {
		(periods map (RollupScanRequest(customer, location, _))).toTraversable
	}

}



