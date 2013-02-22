package com.os.util

import java.lang.management.ManagementFactory
import javax.management.ObjectName

// The defined MBean Interface
trait SupervisorMBean {
	def getAlive():Int
}

/**
 * @author Vadim Bobrov
 */
 class Supervisor extends SupervisorMBean {
	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("JMXSandbox:name=Supervisor"))

	@scala.beans.BeanProperty
	var alive=4
}
