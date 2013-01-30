package com.os.data

import org.scalatest.FunSuite
import com.os.{DataGenerator, MeasurementMessageSender}
import com.os.util.Timing
import com.os.measurement.Measurement

/**
 * @author Vadim Bobrov
 */
class MessageSendingTest extends FunSuite with Timing{

	test("message sending and receiving") {
		MeasurementMessageSender.start()
		DataGenerator.dailyDataIterator(20) foreach  (MeasurementMessageSender.send _)
		MeasurementMessageSender.stop()
	}


}
