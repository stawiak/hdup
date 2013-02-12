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
	 * apply all filters to results of readRequest
	 * @param tvs collection to filter
	 * @return boolean to be used in filter operations
	 */
	def include(tvs: Traversable[TimedValue]): Traversable[TimedValue] = {
		if (filters.isEmpty)
			tvs
		else
			tvs filter (filters reduce ((a, b) => { (x: TimedValue) => a.apply(x) && b.apply(x) }))
	}

	/**
	 * add all string and value literals to retrieved values
	 * @param tvs values to add literals to
	 * @return map of properties to values
	 */
	def enrich(tvs: Traversable[TimedValue]): Traversable[immutable.Map[String, Any]] = {

		def enrichValue(tv: TimedValue): immutable.Map[String, Any] = {
			val map: mutable.Map[String, Any] = mutable.HashMap.empty[String, Any]
			map += ("timestamp" -> tv.timestamp)
			map += ("value" -> tv.value)

			literals foreach (l => l match {
				case s: MQLColumnStringLiteral =>
					map += (s.value -> s.value)
			})

			map.toMap
		}

		tvs map (enrichValue(_))
	}
}
