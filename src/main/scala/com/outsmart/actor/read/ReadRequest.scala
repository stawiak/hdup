package com.outsmart.actor.read

/**
 * @author Vadim Bobrov
 */
abstract case class ReadRequest() {
	def scanRequests() : List[ScanRequest]
}

abstract case class ScanRequest()

case class MeasurementScanRequest(customer : String, location : String, wireid: String, period : (String, String)) extends ScanRequest
case class RollupScanRequest(customer : String, location : String, period : (String, String)) extends ScanRequest

case class MeasurementReadRequest(customer : String, location : String, wireid: String, periods : List[(String, String)]) extends ReadRequest {

	override def scanRequests() : List[ScanRequest] = {
		periods map (MeasurementScanRequest(customer, location, wireid, _))
	}

}

case class RollupReadRequest(customer : String, location : String, periods : List[(String, String)]) extends ReadRequest {

	override def scanRequests() : List[ScanRequest] = {
		periods map (RollupScanRequest(customer, location, _))
	}

}



