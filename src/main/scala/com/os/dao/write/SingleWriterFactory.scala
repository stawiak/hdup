package com.os.dao.write

import com.os.Settings
import org.apache.hadoop.hbase.client.{HTableInterface, Put}
import com.os.measurement._
import com.os.util.BytesWrapper._
import com.os.util.{Loggable, BytesWrapper}
import com.os.dao._

/**
 * @author Vadim Bobrov
 */
object SingleWriterFactory {
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
					RowKeyUtils.createRollupRowKey(msmt.customer, msmt.location, msmt.timestamp)
				else
					RowKeyUtils.createRowKey(msmt.customer, msmt.location, msmt.wireid, msmt.timestamp)

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

	object InterpolatorStateWriterFactory extends WriterFactory {
		override val id: Int = 6
		override val name = "istate"
		override def batchSize = Settings().SmallBatchSize

		def createWriter: Writer = new AbstractWriterWithTableCleanout(Settings.InterpolatorStateTableName) with Loggable {
			def write(obj: AnyRef) {
				val states = obj.asInstanceOf[TimeWindowState]

				states.aggs foreach { state =>
					val p = new Put(state.customer << RowKeyUtils.Separator << state.location)

					state.interpolatorStates foreach { item =>
						val (name, queue) = item
						val content = queue.content()

						if (content.size != 0) {
							val interpolatorValues = content map(BytesWrapper.pimpBytes(_)) reduce(_ << _)
							p.add(Settings.InterpolatorStateColumnFamilyName, name, interpolatorValues)
						}
					}

					table.put(p)
				}
			}
		}

	}


	private abstract class AbstractWriter(private val tableName : String) extends Writer {
		protected var table: HTableInterface = _

		def open() { table = TableFactory(tableName) }

		def close()  {
			table.flushCommits()
			table.close()
		}
	}

	private abstract class AbstractWriterWithTableCleanout(private val tableName : String) extends Writer with Loggable{
		protected var table: HTableInterface = _

		def open() {
			log.info("dropping and recreating table {}", tableName)
			dropRecreateTable()
			log.info("table recreated {}", tableName)
			table = TableFactory(tableName)
		}

		def close()  {
			table.flushCommits()
			table.close()
		}

		private def dropRecreateTable() {
			// drop and recreate table each write
			val tableDescriptor = TableFactory.admin.getTableDescriptor(tableName)
			TableFactory.admin.disableTable(tableName)
			log.debug("table disabled {}", tableName)
			TableFactory.admin.deleteTable(tableName)
			log.debug("table deleted {}", tableName)
			TableFactory.admin.createTable(tableDescriptor)
			log.debug("table created {}", tableName)
		}

	}

}