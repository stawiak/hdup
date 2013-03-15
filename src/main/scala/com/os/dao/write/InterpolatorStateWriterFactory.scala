package com.os.dao.write

import com.os.Settings
import com.os.util.{BytesWrapper, Loggable}
import com.os.dao.{RowKeyUtil, TimeWindowState}
import org.apache.hadoop.hbase.client.Put
import com.os.util.BytesWrapper._

/**
 * @author Vadim Bobrov
 */
object InterpolatorStateWriterFactory extends WriterFactory {
	override val id: Int = 6
	override val name = "istate"
	override def batchSize = Settings().SmallBatchSize

	def createWriter: Writer = new AbstractWriterWithTableCleanout(Settings.InterpolatorStateTableName) with Loggable {
		def write(obj: AnyRef) {
			val states = obj.asInstanceOf[TimeWindowState]

			states.aggs foreach { state =>
				val p = new Put(state.customer << RowKeyUtil.Separator << state.location)

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
