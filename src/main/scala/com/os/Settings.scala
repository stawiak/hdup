package com.os

import concurrent.duration._
import com.typesafe.config.Config
import management.ManagementFactory
import javax.management.ObjectName

/**
 * Wrapper around Config so values can be taken as globals
 * @author Vadim Bobrov
 */
object Settings {
	// non-configurable items
	val TableName = "msmt"              								// table for actual energy measurements
	val MinuteInterpolatedTableName = "ismt" 					 		// table for minute interpolation
	val RollupTableName = "rsmt"			    						// table for minute rollup by location

	val CurrentTableName = "curr"              							// table for actual current measurements
	val VampsTableName = "vamps"              							// table for actual vamps measurements

	val InterpolatorStateTableName = "istate"							// table for interpolator state

	val ColumnFamilyName = "d"      									// stands for data
	val ValueQualifierName = "v"   										// stands for value


	var instance: Option[Settings] = None

	def init(config: Config): Settings = {
		instance = Some(new Settings(config))
		instance.get
	}

	def apply(): Settings = {
		if (!instance.isDefined)
			throw new IllegalAccessException("Settings must be initialized first")

		instance.get
	}

}
trait SettingsMBean {
	def getHttpPort:Int
	def getActiveMQHost:String
	def getActiveMQPort:Int
	def getActiveMQQueue:String
	def getHBaseHost:String
	def getScanCacheSize:Int
	def getBatchSize:Int
	def getDerivedDataBatchSize:Int
	def getTablePoolSize:Int
	def getExpiredTimeWindow:Long
	def getTimeWindowProcessInterval:Long
	def getInterpolation:Boolean
	def getReadTimeout:Long
}
final class Settings(config: Config) extends SettingsMBean {
	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("settings:name=data"))

	val activeMQConfig = config.getConfig("activemq")
	val hBaseConfig = config.getConfig("hbase")

	@scala.beans.BeanProperty
	val HttpPort = config.getInt("port")
	@scala.beans.BeanProperty
	val HttpHost = config.getString("host")

	@scala.beans.BeanProperty
	val ActiveMQHost = activeMQConfig.getString("host")
	@scala.beans.BeanProperty
	val ActiveMQPort = activeMQConfig.getInt("port")
	@scala.beans.BeanProperty
	val ActiveMQQueue = activeMQConfig.getString("queue")

	@scala.beans.BeanProperty
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
	@scala.beans.BeanProperty
	val ScanCacheSize = hBaseConfig.getInt("scanCacheSize")    									// how many rows are retrieved with every RPC call

	@scala.beans.BeanProperty
	val BatchSize = hBaseConfig.getInt("batchSize")            									// default writer batch size - can be lost
	@scala.beans.BeanProperty
	val DerivedDataBatchSize = hBaseConfig.getInt("interpolatedBatchSize")						// writer batch size for minute interpolations- can be lost

	@scala.beans.BeanProperty
	val TablePoolSize = hBaseConfig.getInt("tablePoolSize")


	val ExpiredTimeWindow = Duration(config.getMilliseconds("expiredTimeWindow"), MILLISECONDS) // time to incoming measurement expiration in milliseconds
	def getExpiredTimeWindow = ExpiredTimeWindow.toMillis
	// measurements older than that are not interpolated


	val TimeWindowProcessInterval = FiniteDuration(config.getMilliseconds("timeWindowProcessInterval"), MILLISECONDS)
	def getTimeWindowProcessInterval = TimeWindowProcessInterval.toMillis
	// time between time window processing

	@scala.beans.BeanProperty
	val Interpolation = config.getBoolean("interpolation")


	val ReadTimeout = FiniteDuration(config.getMilliseconds("readTimeout"), MILLISECONDS)
	def getReadTimeout = ReadTimeout.toMillis
}
