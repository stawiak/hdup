package com.os.dao.nest

import com.os.actor.read.{LoadState, InterpolatorStateReadRequest, RollupReadRequest, MeasurementReadRequest}
import com.os.Settings
import com.os.dao.read.{Scanner, Reader, ReaderFactory}

/**
 * @author Vadim Bobrov
 */
object NestReaderFactory {
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
		override val name = "msmt"

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
		override val name = "rollup"

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
		override val name = "istate"

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
		override val name = "tstate"

		def createReader: Reader = new Reader {
			def read(readRequest: AnyRef):AnyRef = {
				val scanner = Scanner(Settings.InterpolatorStateTableName)
				scanner.scanInterpolatorStates
			}
		}
	}


}
