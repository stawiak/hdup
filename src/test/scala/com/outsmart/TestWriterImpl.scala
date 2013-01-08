package com.outsmart

import dao.Writer
import com.outsmart.measurement.Measurement

/**
 * @author Vadim Bobrov
 */
class TestWriterImpl extends Writer {

	def open() {}

	def write(msmt: Measurement) { TestWriterImpl.counter += 1 }

	def close()  {}

}

object TestWriterImpl {
	@volatile var counter = 0
}
