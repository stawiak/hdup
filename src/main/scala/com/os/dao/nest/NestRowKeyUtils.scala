package com.os.dao.nest

import org.apache.hadoop.hbase.util.Bytes
import scala.Byte
import com.os.dao.RowKeyUtil

/**
 * @author Vadim Bobrov
 */
object NestRowKeyUtils extends RowKeyUtil {


	/**
	 * create rowkey using customer, location and reversed timestamp
	 * @param customer
	 * @param location
	 * @param timestamp
	 * @return
	 */
	def createRowKey(customer : String, location : String, timestamp : Long) : Array[Byte] = {

		val rowkey = new Array[Byte](SIZEOF_STRING + SIZEOF_STRING + Bytes.SIZEOF_LONG)

		Bytes.putBytes(rowkey, 0, getHash(customer), 0, SIZEOF_STRING)
		Bytes.putBytes(rowkey, SIZEOF_STRING, getHash(location), 0, SIZEOF_STRING)

		val reverseTimestamp = Long.MaxValue - timestamp
		Bytes.putLong(rowkey, SIZEOF_STRING + SIZEOF_STRING, reverseTimestamp)

		rowkey
	}

	/**
	 * extract timestamp from rowkey bytes
	 * @param rowkey bytes to extract from
	 * @return
	 */
	def getTimestamp(rowkey : Array[Byte]) : Long = {
		val reverseTimestamp = Bytes.toLong(rowkey, SIZEOF_STRING + SIZEOF_STRING)
		Long.MaxValue - reverseTimestamp
	}


}
