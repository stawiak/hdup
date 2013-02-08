package com.os.util

/**
 * @author Vadim Bobrov
 */
trait Timing extends Loggable {

	def time[T, R](f: => T, res:(T, Long) => R): R = {
		val startTime = System.currentTimeMillis
		res(f, System.currentTimeMillis - startTime)
	}

	def time(f: => Unit, msg: String = "execution took") {
		val startTime = System.currentTimeMillis
		f
		log.debug(msg + " " + (System.currentTimeMillis - startTime) + " ms")
	}

}