package com.os

import dao.{AggregatorState, Scanner}
import measurement.TimedValue

/**
 * @author Vadim Bobrov
 */
class TestScannerImpl extends Scanner{
	def scan(customer: String, location: String, wireid: String, start: Long, end: Long): Iterable[TimedValue] = {
		List(new TimedValue(1,1),new TimedValue(2,2),new TimedValue(3,3))
	}

	def scan(customer: String, location: String, start: Long, end: Long): Iterable[TimedValue] = {
		List(new TimedValue(1,1),new TimedValue(2,2),new TimedValue(3,3))
	}

	def scanInterpolatorStates: Map[(String, String), AggregatorState] = null
}
