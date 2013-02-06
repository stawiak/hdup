package com.os

import concurrent.duration._
import com.typesafe.config.Config

/**
 * Wrapper around Config so values can be taken as globals
 * @author Vadim Bobrov
 */
object Settings {

	var instance: Option[Settings] = None

	def apply(config: Config): Settings = {
		if (!instance.isDefined)
			instance = Some(new Settings(config))

		instance.get
	}

}
final class Settings(config: Config) {

	val activeMQConfig = config.getConfig("activemq")
	val hBaseConfig = config.getConfig("hbase")

	val HttpPort = config.getInt("port")
	val HttpHost = config.getString("host")


	val ActiveMQHost = activeMQConfig.getString("host")
	val ActiveMQPort = activeMQConfig.getInt("port")
	val ActiveMQQueue = activeMQConfig.getString("queue")


	val HBaseHost = hBaseConfig.getString("host")
	/*
	  You may need to find a sweet spot between a low number of RPCs and the memory
	  used on the client and server. Setting the scanner caching higher will improve scanning
		performance most of the time, but setting it too high can have adverse effects as well:
		each call to next() will take longer as more data is fetched and needs to be transported
		  to the client, and once you exceed the maximum heap the client process has available
		it may terminate with an OutOfMemoryException.
	  When the time taken to transfer the rows to the client, or to process the
		data on the client, exceeds the configured scanner lease threshold, you
	  will end up receiving a lease expired error, in the form of a Scan
		nerTimeoutException being thrown.
	  */
	val ScanCacheSize = hBaseConfig.getInt("scanCacheSize")    									// how many rows are retrieved with every RPC call


	val TableName = hBaseConfig.getString("tableName")              							// table for actual energy measurements
	val MinuteInterpolatedTableName = hBaseConfig.getString("minuteInterpolatedTableName")  	// table for minute interpolation
	val RollupTableName = hBaseConfig.getString("rollupTableName")			    				// table for minute rollup by location

  val CurrentTableName = hBaseConfig.getString("currentTableName")              							// table for actual current measurements
  val VampsTableName = hBaseConfig.getString("vampsTableName")              							// table for actual vamps measurements


	val ColumnFamilyName = hBaseConfig.getString("columnFamilyName")      						// stands for data
	val ValueQualifierName = hBaseConfig.getString("valueQualifierName")   					// stands for value

	val BatchSize = hBaseConfig.getInt("batchSize")            									// default writer batch size - can be lost
	val DerivedDataBatchSize = hBaseConfig.getInt("interpolatedBatchSize")						// writer batch size for minute interpolations- can be lost

	val TablePoolSize = hBaseConfig.getInt("tablePoolSize")

	val ExpiredTimeWindow = Duration(config.getMilliseconds("expiredTimeWindow"), MILLISECONDS) // time to incoming measurement expiration in milliseconds
																								// measurements older than that are not interpolated

	val TimeWindowProcessInterval = FiniteDuration(config.getMilliseconds("timeWindowProcessInterval"), MILLISECONDS)
																								// time between time window processing

  val Interpolation = config.getBoolean("interpolation")
}
