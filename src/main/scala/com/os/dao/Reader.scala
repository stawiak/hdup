package com.os.dao

import com.os.actor.read.{LoadState, InterpolatorStateReadRequest, RollupReadRequest, MeasurementReadRequest}
import com.os.Settings

/**
 * @author Vadim Bobrov
 */
trait Reader {
	def read(readRequest: AnyRef): AnyRef
}

trait ReaderFactory {
	val id: Int
	def createReader: Reader
}

object ReaderFactory {
	def apply(obj: AnyRef): ReaderFactory = {
		// returned concrete factory based on object type
		obj match {
			case _: MeasurementReadRequest => MeasurementReaderFactory
			case _: RollupReadRequest => RollupReaderFactory
			case _: InterpolatorStateReadRequest => InterpolatorStateReaderFactory
			case _: LoadState => TimeWindowStateReaderFactory
		}
	}


	object MeasurementReaderFactory extends ReaderFactory {
		override val id: Int = 1

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val request = readRequest.asInstanceOf[MeasurementReadRequest]
				val scanner = Scanner(request.tableName)
				scanner.scan(request.customer, request.location, request.wireid, request.period)
			}
		}
	}

	object RollupReaderFactory extends ReaderFactory {
		override val id: Int = 2

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val request = readRequest.asInstanceOf[RollupReadRequest]
				val scanner = Scanner(Settings.RollupTableName)
				scanner.scan(request.customer, request.location, request.period)
			}
		}
	}


	object InterpolatorStateReaderFactory extends ReaderFactory {
		override val id: Int = 6

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				null

				/*
							val rowkey = 	RowKeyUtils.createRollupRowKey(msmt.customer, msmt.location)

							val p = new Put(rowkey)

							p.add(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.ValueQualifierName),Bytes.toBytes(msmt.value))
							table.put(p)
				*/
			}
		}
	}

	object TimeWindowStateReaderFactory extends ReaderFactory {
		override val id: Int = 6

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val scanner = Scanner(Settings.InterpolatorStateTableName)
				scanner.scanInterpolatorStates
			}
		}
	}

}


