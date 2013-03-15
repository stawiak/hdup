package com.os.dao.read

import com.os.dao.{RowKeyUtil, TableFactory, AggregatorState}
import com.os.Settings
import org.apache.hadoop.hbase.client.Scan
import com.os.util.BytesWrapper._
import com.os.interpolation.{NQueueImpl, NQueue}

/**
 * @author Vadim Bobrov
 */
abstract class AbstractScanner {

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
			val (customer, location) = RowKeyUtil.split(res.getRow)

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
