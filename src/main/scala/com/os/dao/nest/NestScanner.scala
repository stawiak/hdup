package com.os.dao.nest

import com.os.measurement.TimedValue
import com.os.dao.nest.NestRowKeyUtils._
import collection.mutable.ListBuffer
import org.apache.hadoop.hbase.client.Scan
import com.os.Settings
import com.os.util.BytesWrapper._
import com.os.dao.read.{AbstractScanner, Scanner}
import com.os.dao.TableFactory
import com.os.util.BytesWrapper

/**
 * @author Vadim Bobrov
 */
object NestScanner {
	def apply(tableName: String) : Scanner = new ScannerImpl(tableName)


	private class ScannerImpl(private val tableName: String) extends AbstractScanner with Scanner {

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

		def scan(customer : String, location : String, wireid : String, start : Long, end : Long): Iterable[TimedValue] = {

			val table = TableFactory(tableName)
			val output = new ListBuffer[TimedValue]()

			val startRowKey = createRowKey(customer, location, end)
			val endRowKey = createRowKey(customer, location, start)

			val scan = new Scan(startRowKey, endRowKey)

			scan.addColumn(Settings.ColumnFamilyName, wireid)

			//TODO: we cannot filter out those outside of time range because we need to take all data from a nest
			//val filter = new ValueFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(".4"))
			//scan.setFilter(filter)

			// how many rows are retrieved with every RPC call
			scan.setCaching(Settings().ScanCacheSize)

			val results = table.getScanner(scan)
			val iterator = Iterator.continually(results.next()) takeWhile (_ != null)

			iterator foreach (res => {
				output ++=  BytesWrapper(res.getValue(Settings.ColumnFamilyName, wireid))
			})

			results.close()
			output filter ( tv => tv.timestamp >= start && tv.timestamp <= end )
		}

		def scan(customer : String, location : String, start : Long, end : Long): Iterable[TimedValue] = {

			val table = TableFactory(tableName)
			val output = new ListBuffer[TimedValue]()

			val startRowKey = createRollupRowKey(customer, location, end)
			val endRowKey = createRollupRowKey(customer, location, start)

			val scan = new Scan(startRowKey, endRowKey)

			scan.addColumn(Settings.ColumnFamilyName, Settings.ValueQualifierName)

			// how many rows are retrieved with every RPC call
			scan.setCaching(Settings().ScanCacheSize)

			val results = table.getScanner(scan)
			val iterator = Iterator.continually(results.next()) takeWhile (_ != null)

			iterator foreach (res => {
				val energy = res.getValue(Settings.ColumnFamilyName, Settings.ValueQualifierName)

				val rowkey = res.getRow
				output += new TimedValue(getTimestampFromRollup(rowkey), energy)
			})

			results.close()
			output
		}

	}


}
