package com.os

import dao.Writer


/**
 * @author Vadim Bobrov
 */
class TestWriterImpl extends Writer {

	def open() {}

	def write(obj: AnyRef) { TestWriterImpl.counter += 1 }

	def close()  {}

}

object TestWriterImpl {
	@volatile var counter = 0
}
