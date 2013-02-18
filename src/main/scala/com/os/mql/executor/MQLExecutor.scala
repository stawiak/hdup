package com.os.mql.executor

import com.os.actor.read.ReadRequest
import com.os.Settings
import org.joda.time.Interval
import com.os.mql.model._
import com.os.measurement.TimedValue
import com.os.actor.read.MeasurementReadRequest
import com.os.mql.model.MQLQuery
import com.os.mql.model.MQLColumnStringLiteral
import com.os.mql.model.MQLValueCondition
import com.os.actor.read.RollupReadRequest
import com.os.mql.model.MQLTimeRangeCondition
import scala.Some
import com.os.mql.model.MQLUnion

/**
 * @author Vadim Bobrov
 */
class MQLExecutor(val mql: MQLUnion) {

	def generateExecutePlan: Traversable[MQLCommand] = {
		mql.queries flatMap (generateQueryPlan(_))
	}


	private def generateQueryPlan(query: MQLQuery): Traversable[MQLCommand] = {
		var output = List.empty[MQLCommand]

		//TODO: what to do if where is not defined
		val request: Option[ReadRequest] =	if (query.where.isDefined) {
				//TODO: enforce single period
				val timeRanges = query.where.get.conds collect { case x: MQLTimeRangeCondition => x }
				val interval = timeRanges map (x => { new Interval(x.startValue.toLong, x.endValue.toLong) }) head //TODO: remove head

			    Some(
					query.from.table match {

						case MQLTableEnergy =>
							new MeasurementReadRequest(Settings.TableName,
								query.where.get.customerCondition.get.value,
								query.where.get.locationCondition.get.value,
								query.where.get.wireidCondition.get.value,
								interval)

						case MQLTableCurrent =>
							new MeasurementReadRequest(Settings.CurrentTableName,
								query.where.get.customerCondition.get.value,
								query.where.get.locationCondition.get.value,
								query.where.get.wireidCondition.get.value,
								interval)

						case MQLTableVamps =>
							new MeasurementReadRequest(Settings.VampsTableName,
								query.where.get.customerCondition.get.value,
								query.where.get.locationCondition.get.value,
								query.where.get.wireidCondition.get.value,
								interval)

						case MQLTableInterpolated =>
							new MeasurementReadRequest(Settings.MinuteInterpolatedTableName,
								query.where.get.customerCondition.get.value,
								query.where.get.locationCondition.get.value,
								query.where.get.wireidCondition.get.value,
								interval)

						case MQLTableRollup =>
							new RollupReadRequest(
								query.where.get.customerCondition.get.value,
								query.where.get.locationCondition.get.value,
								interval)
				})
			} else
				None

		val literals = query.select.columns collect { case x: MQLColumnStringLiteral => x }
		val filters = 	if(query.where.isDefined)
							Some(query.where.get.conds collect { case x: MQLValueCondition => x } map( createFilter(_) ))
						else
							None

		if(query.where.isDefined)
			output = new MQLCommand(request.get,filters.get,literals) :: output

		output
	}

	private def createFilter(cond: MQLValueCondition): (TimedValue) => Boolean = {
		cond.cmp match {
			case "=" => 	{ _.value == cond.value }
			case ">" => 	{ _.value > cond.value }
			case "<" => 	{ _.value < cond.value }
			case ">=" => 	{ _.value >= cond.value }
			case "<=" => 	{ _.value <= cond.value }
		}
	}

}
