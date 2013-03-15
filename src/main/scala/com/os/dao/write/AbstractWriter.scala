package com.os.dao.write

import org.apache.hadoop.hbase.client.HTableInterface
import com.os.dao.TableFactory

/**
 * @author Vadim Bobrov
 */
abstract class AbstractWriter(private val tableName : String) extends Writer {
	protected var table: HTableInterface = _

	def open() { table = TableFactory(tableName) }

	def close()  {
		table.flushCommits()
		table.close()
	}
}
