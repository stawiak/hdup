package com.os.mql.model

import com.os.actor.read.{MeasurementReadRequest, ReadRequest}

/**
 * @author Vadim Bobrov
 */
class MQLExecutor(val mql: MQLUnion) {

	def generate: Traversable[ReadRequest] = {
		mql.queries flatMap (generateQuery(_))
	}


	def generateQuery(query: MQLQuery): Traversable[ReadRequest] = {
		val output = List.empty[ReadRequest]

/*
		var request: ReadRequest = null

		query.from.table match {
			case MQLTableEnergy =>
				null
				//request = new MeasurementReadRequest()
		}
*/

		output
	}


}
