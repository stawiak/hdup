package com.outsmart.actor.read

/**
 * @author Vadim Bobrov
 */
case class MeasurementScanRequest(customer : String, location : String, wireid: String, period : (String, String))
case class RollupScanRequest(customer : String, location : String, period : (String, String))

case class MeasurementReadRequest(customer : String, location : String, wireid: String, periods : List[(String, String)]) {

	def scanRequests() = {
		periods map (MeasurementScanRequest(customer, location, wireid, _))
	}

}

case class RollupReadRequest(customer : String, location : String, periods : List[(String, String)]) {

	def scanRequests() = {
		periods map (RollupScanRequest(customer, location, _))
	}

}



