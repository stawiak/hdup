package com.os.dao.write

/**
 * @author Vadim Bobrov
 */
trait Saveable
trait Writer {
	def open()
	def write(obj: AnyRef)
	def close()
}

trait WriterFactory {
	def batchSize: Int
	val id: Int
	val name: String
	def createWriter: Writer
}
