package com.os

import dao.Scanner
import measurement.MeasuredValue

/**
 * @author Vadim Bobrov
 */
class TestScannerImpl extends Scanner{
	def scan(customer: String, location: String, wireid: String, start: Long, end: Long): List[MeasuredValue] = {
		List(new MeasuredValue(1,1,1,1),new MeasuredValue(2,2,2,2),new MeasuredValue(3,3,3,3))
	}

	def scan(customer: String, location: String, start: Long, end: Long): List[MeasuredValue] = {
		List(new MeasuredValue(1,1,1,1),new MeasuredValue(2,2,2,2),new MeasuredValue(3,3,3,3))
	}
}
