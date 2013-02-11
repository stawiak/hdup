package com.os.mql.model

import com.os.actor.read.{MeasurementReadRequest, ReadRequest}
import com.os.Settings
import org.joda.time.Interval

/**
 * @author Vadim Bobrov
 */
class MQLExecutor(val mql: MQLUnion) {

	def generate: Traversable[ReadRequest] = {
		mql.queries flatMap (generateQuery(_))
	}


	private def generateQuery(query: MQLQuery): Traversable[ReadRequest] = {
		var output = List.empty[ReadRequest]


		var request: ReadRequest = null
		var intervals: Traversable[Interval] = null

		if (query.where.isDefined) {
			val timeRanges = query.where.get.conds collect { case x: MQLTimeRangeCondition => x }
			intervals = timeRanges map (x => {
				new Interval(x.startValue.toLong, x.endValue.toLong)
			})
		}

		query.from.table match {
			case MQLTableEnergy() =>
				output = new MeasurementReadRequest(Settings.TableName,
					query.where.get.customerCondition.get.value,
					query.where.get.locationCondition.get.value,
					query.where.get.locationCondition.get.value,
				    intervals
				) :: output
		}


		output
	}


}
