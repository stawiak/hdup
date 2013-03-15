package com.os.dao.write

import com.os.util.Loggable
import org.apache.hadoop.hbase.client.HTableInterface
import com.os.dao.TableFactory
import com.os.util.BytesWrapper._

/**
 * @author Vadim Bobrov
 */
abstract class AbstractWriterWithTableCleanout(private val tableName : String) extends Writer with Loggable{
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
