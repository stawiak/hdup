package com.outsmart

import dao.Scanner
import measurement.MeasuredValue

/**
 * @author Vadim Bobrov
 */
class TestScanerImpl extends Scanner{
	def scan(customer: String, location: String, wireid: String, start: Long, end: Long): Array[MeasuredValue] = {
		Array(new MeasuredValue(1,1,1,1),new MeasuredValue(2,2,2,2),new MeasuredValue(3,3,3,3))
	}
}
