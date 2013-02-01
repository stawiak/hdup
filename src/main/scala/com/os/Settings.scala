package com.os

import concurrent.duration._
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
object Settings {

	val config = ConfigFactory.load().getConfig("prod")

	val ActiveMQHOst = config.getString("activemq.host")

	val HttpPort = config.getInt("port")
	val HttpHost = config.getString("host")

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
	val ScanCacheSize = 1000        			// how many rows are retrieved with every RPC call


	val TableName = "msmt"              		// table for actual measurements
	val MinuteInterpolaedTableName = "ismt"    	// table for minute interpolation
	val RollupTableName = "rsmt"    			// table for minute rollup by location


	val ColumnFamilyName = "d"      			// stands for data
	val EnergyQualifierName = "e"   			// stands for energy
	val CurrentQualifierName = "c"  			// stands for current
	val VampireQualifierName = "v"  			// stands for volt-amp-reactive

	val BatchSize = 1000             			// default writer batch size - can be lost
	val DerivedDataBatchSize = 10		// writer batch size for minute interpolations- can be lost

	val TablePoolSize = 100

	val Host = "node0"
	//val Host = "192.168.152.128"
	//val Host = "10.0.0.158"

	val ExpiredTimeWindow = 570000				// time to incoming measurement expiration in milliseconds
												// measurements older than that are not interpolated

	val TimeWindowProcessInterval = 10 second	// time between time window processing

}
