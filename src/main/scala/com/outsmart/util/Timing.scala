package com.outsmart.util

/**
 * @author Vadim Bobrov
 */
trait Timing extends Loggable {

	def time[T, R](f: => T, res:(T, Long) => R): R = {
		val startTime = System.currentTimeMillis
		res(f, System.currentTimeMillis - startTime)
	}

	def time(f: => Unit) {
		val startTime = System.currentTimeMillis
		f
		logger.debug("execution took " + (System.currentTimeMillis - startTime) + " ms")
	}

}