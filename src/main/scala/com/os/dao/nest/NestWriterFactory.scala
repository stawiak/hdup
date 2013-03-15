package com.os.dao.nest

import com.os.Settings
import org.apache.hadoop.hbase.client.Put
import com.os.measurement._
import com.os.util.BytesWrapper._
import com.os.dao._
import clwt.CLWTRowKeyUtils
import write._

/**
 * A writer factory that stores each measurement in a separate cell
 * Rowkey - hash(customer) hash(location) hash(wireid) reverse timestamp -> value
 * column qualifier and hbase timestamp are not used
 * @author Vadim Bobrov
 */
object NestWriterFactory {
	def apply(obj: AnyRef): WriterFactory = {
		// returned concrete factory based on object type
		obj match {
			// rollup and interpolated should be matched before
			// other measurement classes or they will be swallowed
			case _: Rollup => RollupMeasurementWriterFactory
			case _: Interpolated => InterpolatedMeasurementWriterFactory

			case _: TimeWindowState => InterpolatorStateWriterFactory
			case _: EnergyMeasurement => EnergyMeasurementWriterFactory
			case _: CurrentMeasurement => CurrentMeasurementWriterFactory
			case _: VampsMeasurement => VampsMeasurementWriterFactory
		}
	}


	abstract class AbstractMeasurementWriterFactory(val tableName: String) extends WriterFactory{
		override val name = tableName

		def createWriter: Writer = new AbstractWriter(tableName) {
			/**
			(alternatively see Batch operations to group updates)

			  Those Put instances that have failed on the server side are kept in the local write buffer.
			They will be retried the next time the buffer is flushed. You can also access them using
			the getWriteBuffer() method of HTable and take, for example, evasive actions.
			Some checks are done on the client side, though—for example, to ensure that the put
			has a column specified or that it is completely empty. In that event, the client is throwing
			an exception that leaves the operations preceding the faulty one in the client buffer.

			The list-based put() call uses the client-side write buffer to insert all puts
			into the local buffer and then to call flushCache() implicitly. While
			inserting each instance of Put, the client API performs the mentioned
			check. If it fails, for example, at the third put out of five—the first two
			are added to the buffer while the last two are not. It also then does not
			trigger the flush command at all.

			You need to watch out for a peculiarity using the list-based put call: you cannot control
			the order in which the puts are applied on the server side, which implies that the order
			in which the servers are called is also not under your control. Use this call with caution
			if you have to guarantee a specific order—in the worst case, you need to create smaller
			batches and explicitly flush the client-side write cache to enforce that they are sent to
			the remote servers
			  */

			def write(obj: AnyRef) {
				val msmt = obj.asInstanceOf[Measurement]
				val rowkey = if(tableName == Settings.RollupTableName)
					CLWTRowKeyUtils.createRollupRowKey(msmt.customer, msmt.location, msmt.timestamp)
				else
					CLWTRowKeyUtils.createRowKey(msmt.customer, msmt.location, msmt.wireid, msmt.timestamp)

				val p = new Put(rowkey)

				p.add(Settings.ColumnFamilyName, Settings.ValueQualifierName, msmt.value)
				//p.add(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.CurrentQualifierName),Bytes.toBytes(msmt.current))
				//p.add(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.VampireQualifierName),Bytes.toBytes(msmt.vampire))
				// alternatively use
				// void put(List<Put> puts) throws IOException
				table.put(p)
			}
		}
	}

	object EnergyMeasurementWriterFactory extends AbstractMeasurementWriterFactory(Settings.EnergyTableName) {
		override val id = 1
		override def batchSize = Settings().LargeBatchSize
	}

	object CurrentMeasurementWriterFactory extends AbstractMeasurementWriterFactory(Settings.CurrentTableName) {
		override val id = 2
		override def batchSize = Settings().LargeBatchSize
	}

	object VampsMeasurementWriterFactory extends AbstractMeasurementWriterFactory(Settings.VampsTableName) {
		override val id = 3
		override def batchSize = Settings().LargeBatchSize
	}

	object RollupMeasurementWriterFactory extends AbstractMeasurementWriterFactory(Settings.RollupTableName) {
		override val id = 4
		override def batchSize = Settings().SmallBatchSize
	}

	object InterpolatedMeasurementWriterFactory extends AbstractMeasurementWriterFactory(Settings.MinuteInterpolatedTableName) {
		override val id = 5
		override def batchSize = Settings().LargeBatchSize
	}

}