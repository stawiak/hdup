package com.os.dao.read


/**
 * @author Vadim Bobrov
 */
trait Reader {
	def read(readRequest: AnyRef): AnyRef
}

trait ReaderFactory {
	val id: Int
	val name: String
	def createReader: Reader
}
