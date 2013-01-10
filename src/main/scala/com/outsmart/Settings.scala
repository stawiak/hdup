package com.outsmart

/**
 * @author Vadim Bobrov
 */
object Settings {

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
	val MinuteInterpolaedTableName = "mimt"    	// table for minute interpolation
	val ColumnFamilyName = "d"      			// stands for data
	val EnergyQualifierName = "e"   			// stands for energy
	val CurrentQualifierName = "c"  			// stands for current
	val VampireQualifierName = "v"  			// stands for volt-amp-reactive

	val BatchSize = 1000             			// default writer batch size - can be lost
	val MinuteInterpolatedBatchSize = 10		// writer batch size for minute interpolations- can be lost

	val TablePoolSize = 100

	val Host = "192.168.152.128"
	//val Host = "10.0.0.158"

	val ExpiredTimeWindow = 570000				// time to incoming measurement expiration in milliseconds
												// measurements older than that are not interpolated
}
