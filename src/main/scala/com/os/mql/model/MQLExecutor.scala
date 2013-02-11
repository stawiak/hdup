package com.os.mql.model

import com.os.actor.read.{RollupReadRequest, MeasurementReadRequest, ReadRequest}
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

		if (query.where.isDefined) {
			val timeRanges = query.where.get.conds collect { case x: MQLTimeRangeCondition => x }
			val intervals = timeRanges map (x => { new Interval(x.startValue.toLong, x.endValue.toLong) })


			query.from.table match {

				case MQLTableEnergy() =>
					output = new MeasurementReadRequest(Settings.TableName,
						query.where.get.customerCondition.get.value,
						query.where.get.locationCondition.get.value,
						query.where.get.wireidCondition.get.value,
						intervals
					) :: output

				case MQLTableCurrent() =>
					output = new MeasurementReadRequest(Settings.CurrentTableName,
						query.where.get.customerCondition.get.value,
						query.where.get.locationCondition.get.value,
						query.where.get.wireidCondition.get.value,
						intervals
					) :: output

				case MQLTableVamps() =>
					output = new MeasurementReadRequest(Settings.VampsTableName,
						query.where.get.customerCondition.get.value,
						query.where.get.locationCondition.get.value,
						query.where.get.wireidCondition.get.value,
						intervals
					) :: output

				case MQLTableInterpolated() =>
					output = new MeasurementReadRequest(Settings.MinuteInterpolatedTableName,
						query.where.get.customerCondition.get.value,
						query.where.get.locationCondition.get.value,
						query.where.get.wireidCondition.get.value,
						intervals
					) :: output

				case MQLTableRollup() =>
					output = new RollupReadRequest(
						query.where.get.customerCondition.get.value,
						query.where.get.locationCondition.get.value,
						intervals
					) :: output


			}
		}

		output
	}


}
