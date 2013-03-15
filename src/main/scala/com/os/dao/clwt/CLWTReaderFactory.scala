package com.os.dao.clwt

import com.os.actor.read.{LoadState, RollupReadRequest, MeasurementReadRequest}
import com.os.Settings
import com.os.dao.read.{Reader, ReaderFactory}

/**
 * @author Vadim Bobrov
 */
object CLWTReaderFactory {
	def apply(obj: AnyRef): ReaderFactory = {
		// returned concrete factory based on object type
		obj match {
			case _: MeasurementReadRequest => MeasurementReaderFactory
			case _: RollupReadRequest => RollupReaderFactory
			case _: LoadState => TimeWindowStateReaderFactory
		}
	}


	object MeasurementReaderFactory extends ReaderFactory {
		override val id: Int = 1
		override val name = "msmt"

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val request = readRequest.asInstanceOf[MeasurementReadRequest]
				val scanner = CLWTScanner(request.tableName)
				scanner.scan(request.customer, request.location, request.wireid, request.period)
			}
		}
	}

	object RollupReaderFactory extends ReaderFactory {
		override val id: Int = 2
		override val name = "rollup"

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val request = readRequest.asInstanceOf[RollupReadRequest]
				val scanner = CLWTScanner(Settings.RollupTableName)
				scanner.scan(request.customer, request.location, request.period)
			}
		}
	}


	object TimeWindowStateReaderFactory extends ReaderFactory {
		override val id: Int = 6
		override val name = "tstate"

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val scanner = CLWTScanner(Settings.InterpolatorStateTableName)
				scanner.scanInterpolatorStates
			}
		}
	}


}
