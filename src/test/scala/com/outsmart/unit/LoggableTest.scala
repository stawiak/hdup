package com.outsmart.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.outsmart.util.{Timing, Timer}

/**
 * @author Vadim Bobrov
 */
class LoggableTest extends FlatSpec with ShouldMatchers with Timing {

	"Loggable trait" should "enable logging correctly" in {
			time({Thread.sleep(50)})
			Timer.periodicCall(1, {debug("aaa")})
		}



}
