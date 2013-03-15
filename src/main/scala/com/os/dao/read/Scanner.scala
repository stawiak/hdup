package com.os.dao.read

import org.joda.time.Interval
import com.os.measurement.TimedValue
import com.os.dao.AggregatorState


/**
 * @author Vadim Bobrov
*/
trait Scanner {
	def scan(customer: String, location: String, wireid: String, period: Interval): Iterable[TimedValue] = {
		scan(customer, location, wireid, period.getStartMillis, period.getEndMillis)
	}

	def scan(customer: String, location: String, wireid: String, start: Long, end: Long): Iterable[TimedValue]

	def scan(customer : String, location : String, period: Interval): Iterable[TimedValue] = {
		scan(customer, location, period.getStartMillis, period.getEndMillis)
	}

	def scan(customer: String, location: String, start: Long, end: Long): Iterable[TimedValue]

	def scanInterpolatorStates: Map[(String, String), AggregatorState]
}
