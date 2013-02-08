package com.os.actor.read

import org.joda.time.Interval

/**
 * @author Vadim Bobrov
 */
abstract class ReadRequest
case class MeasurementScanRequest(tableName: String, customer: String, location: String, wireid: String, period: Interval)
case class MeasurementReadRequest(tableName: String, customer: String, location: String, wireid: String, periods: Array[Interval]) extends ReadRequest {

	def scanRequests() = {
		(periods map (MeasurementScanRequest(tableName, customer, location, wireid, _))).toTraversable
	}

}

case class InterpolatedScanRequest(customer: String, location: String, wireid: String, period: Interval)
case class InterpolatedReadRequest(customer: String, location: String, wireid: String, periods: Array[Interval]) extends ReadRequest {

  def scanRequests() = {
    (periods map (InterpolatedScanRequest(customer, location, wireid, _))).toTraversable
  }

}


case class RollupScanRequest(customer: String, location: String, period: Interval)
case class RollupReadRequest(customer: String, location: String, periods: Array[Interval]) extends ReadRequest {

	def scanRequests() = {
		(periods map (RollupScanRequest(customer, location, _))).toTraversable
	}

}



