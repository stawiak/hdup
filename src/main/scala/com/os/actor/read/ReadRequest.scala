package com.os.actor.read

import org.joda.time.Interval
import com.os.Settings

/**
 * @author Vadim Bobrov
 */
abstract class ReadRequest(val tableName: String, val customer: String, val location: String, val periods: Traversable[Interval])
case class MeasurementScanRequest(tableName: String, customer: String, location: String, wireid: String, period: Interval)
case class MeasurementReadRequest(override val tableName: String, override val customer: String, override val location: String, wireid: String, override val periods: Traversable[Interval]) extends ReadRequest(tableName, customer, location, periods) {

	def scanRequests() = {
		periods map (MeasurementScanRequest(tableName, customer, location, wireid, _))
	}

}

case class InterpolatedScanRequest(customer: String, location: String, wireid: String, period: Interval)
case class InterpolatedReadRequest(override val customer: String, override val location: String, wireid: String, override val periods: Traversable[Interval]) extends ReadRequest(Settings.MinuteInterpolatedTableName, customer, location, periods) {

  def scanRequests() = {
    periods map (InterpolatedScanRequest(customer, location, wireid, _))
  }

}


case class RollupScanRequest(customer: String, location: String, period: Interval)
case class RollupReadRequest(override val customer: String, override val location: String, override val periods: Traversable[Interval]) extends ReadRequest(Settings.RollupTableName, customer, location, periods) {

	def scanRequests() = {
		periods map (RollupScanRequest(customer, location, _))
	}

}



