package com.os.util

/**
 * count occurrences
 * @author Vadim Bobrov
 */
class CounterMap[T] {

	var map = Map[T, Int]()

	def incr(key: T) {
		if (!map.contains(key))
			map += (key -> 1)
		else
			map += (key -> (1 + map(key)))
	}

	override def toString: String = map.mkString(",")
}
