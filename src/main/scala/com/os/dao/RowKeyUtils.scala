package com.os.dao

import org.apache.hadoop.hbase.util.Bytes
import java.security.MessageDigest
import scala.Byte

/**
 * @author Vadim Bobrov
 */
object RowKeyUtils {

	private val SIZEOF_STRING = 16
	val Separator = Bytes.toBytes("^")

	/**
	 * create rowkey using customer, location, wireid and reversed timestamp
	 * @param customer
	 * @param location
	 * @param wireid
	 * @param timestamp
	 * @return
	 */
	def createRowKey(customer : String, location : String, wireid : String, timestamp : Long) : Array[Byte] = {

		val rowkey = new Array[Byte](SIZEOF_STRING + SIZEOF_STRING + SIZEOF_STRING + Bytes.SIZEOF_LONG)

		Bytes.putBytes(rowkey, 0, getHash(customer), 0, SIZEOF_STRING)
		Bytes.putBytes(rowkey, SIZEOF_STRING, getHash(location), 0, SIZEOF_STRING)
		Bytes.putBytes(rowkey, SIZEOF_STRING + SIZEOF_STRING, getHash(wireid), 0, SIZEOF_STRING)

		val reverseTimestamp = Long.MaxValue - timestamp
		Bytes.putLong(rowkey, SIZEOF_STRING + SIZEOF_STRING + SIZEOF_STRING, reverseTimestamp)

		rowkey
	}

	/**
	 * create rowkey using customer, location and reversed timestamp
	 * @param customer
	 * @param location
	 * @param timestamp
	 * @return
	 */
	def createRollupRowKey(customer : String, location : String, timestamp : Long) : Array[Byte] = {

		val rowkey = new Array[Byte](SIZEOF_STRING + SIZEOF_STRING + Bytes.SIZEOF_LONG)

		Bytes.putBytes(rowkey, 0, getHash(customer), 0, SIZEOF_STRING)
		Bytes.putBytes(rowkey, SIZEOF_STRING, getHash(location), 0, SIZEOF_STRING)

		val reverseTimestamp = Long.MaxValue - timestamp
		Bytes.putLong(rowkey, SIZEOF_STRING + SIZEOF_STRING, reverseTimestamp)

		rowkey
	}

	/**
	 * create partial rowkey using only part of the entire key
	 * @param customer
	 * @param location
	 * @return
	 */
	def createRowKeyPrefix(customer : String, location : String) : Array[Byte] = {

		val rowkey = new Array[Byte](SIZEOF_STRING + SIZEOF_STRING)

		Bytes.putBytes(rowkey, 0, getHash(customer), 0, SIZEOF_STRING)
		Bytes.putBytes(rowkey, SIZEOF_STRING, getHash(location), 0, SIZEOF_STRING)

		rowkey
	}


	/*
	  * get a unique (almost) hash for a string to use in row key
	   */
	private def getHash(s : String) : Array[Byte] = {

		val md = MessageDigest.getInstance("MD5")

		md.update(s.getBytes)
		md.digest()
	}

	/**
	 * extract timestamp from rowkey bytes
	 * @param rowkey bytes to extract from
	 * @return
	 */
	def getTimestamp(rowkey : Array[Byte]) : Long = {
		val reverseTimestamp = Bytes.toLong(rowkey, SIZEOF_STRING + SIZEOF_STRING + SIZEOF_STRING)
		Long.MaxValue - reverseTimestamp
	}

	/**
	 * extract timestamp from rowkey bytes
	 * @param rowkey bytes to extract from
	 * @return
	 */
	def getTimestampFromRollup(rowkey : Array[Byte]) : Long = {
		val reverseTimestamp = Bytes.toLong(rowkey, SIZEOF_STRING + SIZEOF_STRING)
		Long.MaxValue - reverseTimestamp
	}

}
