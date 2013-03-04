package com.os.dao

import org.joda.time.Interval
import org.apache.hadoop.hbase.client.Scan
import com.os.Settings
import collection.mutable.ListBuffer
import com.os.measurement.TimedValue
import com.os.util.BytesWrapper._
import com.os.interpolation.{NQueueImpl, NQueue}

/**
 * @author Vadim Bobrov
*/
trait Scanner {
	def scan(customer: String, location: String, wireid: String, period: Interval): Iterable[TimedValue] = {
		scan(customer, location, wireid, period.getStartMillis, period.getEndMillis)
	}

	def scan(customer: String, location: String, wireid: String, start: Long, end: Long): Iterable[TimedValue]

	def scan(customer : String, location : String, period: Interval): Iterable[TimedValue] = {
		scan(customer, location, period.getStartMillis, period.getEndMillis)
	}

	def scan(customer: String, location: String, start: Long, end: Long): Iterable[TimedValue]

	def scanInterpolatorStates: Map[(String, String), AggregatorState]
}

object Scanner {
	def apply(tableName: String) : Scanner = new ScannerImpl(tableName)


	private class ScannerImpl(private val tableName: String) extends Scanner {

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
			val startRowKey = RowKeyUtils.createRowKey(customer, location, wireid, end)
			val endRowKey = RowKeyUtils.createRowKey(customer, location, wireid, start)
			scan(startRowKey, endRowKey, RowKeyUtils.getTimestamp(_) )
		}

		def scan(customer : String, location : String, start : Long, end : Long): Iterable[TimedValue] = {
			val startRowKey = RowKeyUtils.createRollupRowKey(customer, location, end)
			val endRowKey = RowKeyUtils.createRollupRowKey(customer, location, start)
			scan(startRowKey, endRowKey, RowKeyUtils.getTimestampFromRollup(_))
		}

		private def scan(startRowKey: Array[Byte], endRowKey: Array[Byte], timestampExtractor:(Array[Byte]) => Long): Iterable[TimedValue] = {

			val table = TableFactory(tableName)
			val output = new ListBuffer[TimedValue]()
			val scan = new Scan(startRowKey, endRowKey)

			scan.addColumn(Settings.ColumnFamilyName, Settings.ValueQualifierName)
			//scan.addColumn(Bytes.toBytes(settings.ColumnFamilyName), Bytes.toBytes(settings.CurrentQualifierName))
			//scan.addColumn(Bytes.toBytes(settings.ColumnFamilyName), Bytes.toBytes(settings.VampireQualifierName))

			// how many rows are retrieved with every RPC call
			scan.setCaching(Settings().ScanCacheSize)

			val results = table.getScanner(scan)
			val iterator = Iterator.continually(results.next()) takeWhile (_ != null)

			iterator foreach (res => {
				val energy = res.getValue(Settings.ColumnFamilyName, Settings.ValueQualifierName)
				//val current = res.getValue(Bytes.toBytes(settings.ColumnFamilyName), Bytes.toBytes(settings.CurrentQualifierName))
				//val vampire = res.getValue(Bytes.toBytes(settings.ColumnFamilyName), Bytes.toBytes(settings.VampireQualifierName))

				val rowkey = res.getRow
				//output += new MeasuredValue(RowKeyUtils.getTimestamp(row), Bytes.toDouble(energy), Bytes.toDouble(current), Bytes.toDouble(vampire))
				output += new TimedValue(timestampExtractor(rowkey), energy)
			})

			results.close()
			output
		}

		def scanInterpolatorStates: Map[(String, String), AggregatorState] = {
			import scala.collection.JavaConversions._

			val table = TableFactory(Settings.InterpolatorStateTableName)

			var output = Map[(String, String), AggregatorState]()
			val scan = new Scan()

			scan.addFamily(Settings.InterpolatorStateColumnFamilyName)

			// how many rows are retrieved with every RPC call
			scan.setCaching(Settings().ScanCacheSize)

			val results = table.getScanner(scan)
			val iterator = Iterator.continually(results.next()) takeWhile (_ != null)

			iterator foreach (res => {
				val familyMap = res.getFamilyMap(Settings.ColumnFamilyName)
				val interpolatorStates = familyMap.keySet() map {key => ( bytesToString(key), bytesToNQueue(familyMap.get(key)))}
				val (c, l) = res.getRow.span(_ == RowKeyUtils.Separator)
				val customer: String = c
				val location: String = l

				output += ((customer, location) -> new AggregatorState(customer, location, interpolatorStates))
			})

			results.close()
			output
		}

		private def bytesToNQueue(in: Array[Byte]): NQueue = {
			val queue = new NQueueImpl()
			in.extractTimedValues foreach ( queue offer _)
			queue
		}


	}


}
