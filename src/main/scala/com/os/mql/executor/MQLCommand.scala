package com.os.mql.executor

import com.os.actor.read.ReadRequest
import com.os.measurement.TimedValue
import collection.{immutable, mutable}
import com.os.mql.model.MQLColumnStringLiteral


/**
 * @author Vadim Bobrov
 */
class MQLCommand(val readRequest: ReadRequest, val filters: Traversable[TimedValue => Boolean], val literals: Traversable[MQLColumnStringLiteral]) {

	/**
	 * apply all filters to a TimedValue
	 * @param tv value to apply filters
	 * @return boolean to be used in filter operations
	 */
	def include(tv: TimedValue): Boolean = {
		if (filters.isEmpty)
			true
		else
			filters reduce ((a, b) => { (x: TimedValue) => a.apply(x) && b.apply(x) }) apply(tv)
	}

	/**
	 * add all string and value literals to a retrieved value
	 * @param tv value to add literals to
	 * @return map of properties to values
	 */
	def transform(tv: TimedValue): immutable.Map[String, Any] = {
		val map: mutable.Map[String, Any] = mutable.HashMap.empty[String, Any]
		map += ("timestamp" -> tv.timestamp)
		map += ("value" -> tv.value)

		literals foreach (l => l match {
			case s: MQLColumnStringLiteral =>
				map += (s.value -> s.value)
		})

		map.toMap
	}
}
