package com.os.mql.model

import com.os.actor.read.{MeasurementReadRequest, ReadRequest}
import com.os.Settings

/**
 * @author Vadim Bobrov
 */
class MQLExecutor(val mql: MQLUnion) {

	def generate: Traversable[ReadRequest] = {
		mql.queries flatMap (generateQuery(_))
	}


	def generateQuery(query: MQLQuery): Traversable[ReadRequest] = {
		val output = List.empty[ReadRequest]


		var request: ReadRequest = null

/*
		if (query.where.isDefined) {
			query.where.get.
		}

		query.from.table match {
			case MQLTableEnergy =>
				request = new MeasurementReadRequest(Settings.TableName,)
		}
*/

		output
	}


}
