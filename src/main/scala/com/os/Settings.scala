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
	val EnergyTableName = "msmt"           								// table for actual energy measurements
	val MinuteInterpolatedTableName = "ismt" 					 		// table for minute interpolation
	val RollupTableName = "rsmt"			    						// table for minute rollup by location

	val CurrentTableName = "curr"              							// table for actual current measurements
	val VampsTableName = "vamps"              							// table for actual vamps measurements

	val ColumnFamilyName = "d"      									// stands for data
	val ValueQualifierName = "v"   										// stands for value


	val InterpolatorStateTableName = "istate"							// table for interpolator state
	val InterpolatorStateColumnFamilyName = "d"


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

	def getLargeBatchSize:Int
	def setLargeBatchSize(size: Int)

	def getSmallBatchSize:Int
	def setSmallBatchSize(size: Int)

	def getTablePoolSize:Int
	def getExpiredTimeWindow:Long

	def getTimeWindowProcessInterval:Long
	def setTimeWindowProcessInterval(millis: Long)

	def getInterpolation:Boolean
	def getReadTimeout:Long

	def getSaveStateOnShutdown:Boolean
	def setSaveStateOnShutdown(b: Boolean)

	def getLoadStateOnStartup:Boolean
}
final class Settings(val config: Config) extends SettingsMBean {

	val mBeanServer = ManagementFactory.getPlatformMBeanServer
	val jmxName = new ObjectName("com.os.chaos:type=Settings,name=settings")
	if (mBeanServer.isRegistered(jmxName))
		mBeanServer.unregisterMBean(jmxName)

	mBeanServer.registerMBean(this, jmxName)

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


	var LargeBatchSize = hBaseConfig.getInt("largeBatchSize")     								// default writer batch size - can be lost
	def getLargeBatchSize = LargeBatchSize
	def setLargeBatchSize(size: Int) { LargeBatchSize = size }

	var SmallBatchSize = hBaseConfig.getInt("smallBatchSize")									// writer batch size for minute interpolations- can be lost
	def getSmallBatchSize = SmallBatchSize
	def setSmallBatchSize(size: Int) { SmallBatchSize = size }

	val SingleBatchSize = 1

	@scala.beans.BeanProperty
	val TablePoolSize = hBaseConfig.getInt("tablePoolSize")


	val ExpiredTimeWindow = Duration(config.getMilliseconds("expiredTimeWindow"), MILLISECONDS) // time to incoming measurement expiration in milliseconds
	def getExpiredTimeWindow = ExpiredTimeWindow.toMillis
	// measurements older than that are not interpolated


	var TimeWindowProcessInterval = FiniteDuration(config.getMilliseconds("timeWindowProcessInterval"), MILLISECONDS)
	def getTimeWindowProcessInterval = TimeWindowProcessInterval.toMillis
	def setTimeWindowProcessInterval(millis: Long) {
		TimeWindowProcessInterval = FiniteDuration(millis, MILLISECONDS)
	}
	// time between time window processing

	@scala.beans.BeanProperty
	val Interpolation = config.getBoolean("interpolation")


	val ReadTimeout = FiniteDuration(config.getMilliseconds("readTimeout"), MILLISECONDS)
	def getReadTimeout = ReadTimeout.toMillis

	@scala.beans.BeanProperty
	var SaveStateOnShutdown = config.getBoolean("saveStateOnShutdown")

	@scala.beans.BeanProperty
	val LoadStateOnStartup = config.getBoolean("loadStateOnStartup")

}
