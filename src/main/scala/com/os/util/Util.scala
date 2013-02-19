package com.os.util

/**
 * @author Vadim Bobrov
 */
object Util {

	def using[T <: { def open() : Unit; def close() : Unit  }](oc : T)(op: => Unit) {
		oc.open()

		try{
			op
		} finally {
			oc.close()
		}
	}

}
