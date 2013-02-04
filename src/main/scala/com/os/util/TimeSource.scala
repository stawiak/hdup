package com.os.util

/**
 * @author Vadim Bobrov
 */
trait TimeSource {
	def now(): Long = System.currentTimeMillis()
}

