package com.outsmart.dao

import org.joda.time.DateTime
import com.outsmart.measurement.MeasuredValue
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.util.Bytes
import com.outsmart.Settings

/**
 * @author Vadim Bobrov
*/
trait Scanner {
	def scan(customer : String, location : String, wireid : String, start : DateTime, end : DateTime) : List[MeasuredValue] = {
		scan(customer, location, wireid, start.getMillis, end.getMillis)
	}

	def scan(customer : String, location : String, wireid : String, start : Long, end : Long) : List[MeasuredValue]

	def scan(customer : String, location : String, start : DateTime, end : DateTime) : List[MeasuredValue] = {
		scan(customer, location, start.getMillis, end.getMillis)
	}

	def scan(customer : String, location : String, start : Long, end : Long) : List[MeasuredValue]
}

object Scanner {
	def apply(tableName : String = Settings.TableName) : Scanner = new ScannerImpl(tableName)


	private class ScannerImpl(private val tableName : String = Settings.TableName) extends Scanner {

		/**
		Sometimes it might be necessary to find a specific row, or the one just before the re-
    quested row, when retrieving data. The following call can help you find a row using
    these semantics:

      <code>Result getRowOrBefore(byte[] row, byte[] family) throws IOException</code>

    You need to specify the row you are looking for, and a column family. The latter is
    required because, in HBase, which is a column-oriented database, there is no row if
    there are no columns.

    ...all columns of the specified column family were returned, including their latest
    values. You could use this call to     quickly retrieve all the latest values from
    an entire column family—in other words, all columns contained in the given column
    family—based on a specific sorting pattern

    start and end: you do not need to have an exact match for
    either of these rows. Instead, the scan will match the first row key that is equal to or
    larger than the given start row. If no start row was specified, it will start at the beginning
    of the table.
    It will also end its work when the current row key is equal to or greater than the optional
    stop row. If no stop row was specified, the scan will run to the end of the table.
		  */

		def scan(customer : String, location : String, wireid : String, start : Long, end : Long) : List[MeasuredValue] = {

			val table = TableFactory(tableName)

			var output = List[MeasuredValue]()

			val startRowKey = if(tableName == Settings.RollupTableName) RowKeyUtils.createRollupRowKey(customer, location, end) else RowKeyUtils.createRowKey(customer, location, wireid, end)
			val endRowKey = if(tableName == Settings.RollupTableName) RowKeyUtils.createRollupRowKey(customer, location, start) else RowKeyUtils.createRowKey(customer, location, wireid, start)

			val scan = new Scan(startRowKey, endRowKey)

			scan.addColumn(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.EnergyQualifierName))
			scan.addColumn(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.CurrentQualifierName))
			scan.addColumn(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.VampireQualifierName))

			// how many rows are retrieved with every RPC call
			scan.setCaching(Settings.ScanCacheSize)

			val results = table.getScanner(scan)

			var res : Result = null

			val iterator = Iterator.continually(results.next()) takeWhile (_ != null)

			iterator foreach (res => {
				val energy = res.getValue(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.EnergyQualifierName))
				val current = res.getValue(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.CurrentQualifierName))
				val vampire = res.getValue(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.VampireQualifierName))

				val row = res.getRow
				output = new MeasuredValue(RowKeyUtils.getTimestamp(row), Bytes.toDouble(energy), Bytes.toDouble(current), Bytes.toDouble(vampire)) :: output
			})

			results.close()
			output
		}


	}


}
