package com.os.data

import org.scalatest.FunSuite
import com.os.{DataGenerator, MeasurementMessageSender}
import com.os.util.Timing

/**
 * @author Vadim Bobrov
 */
class MessageSendingTest extends FunSuite with Timing{

	val dataGenerator = new DataGenerator
	test("message sending and receiving") {
		MeasurementMessageSender.start()
		time {dataGenerator.dailyDataIterator(20, realTime = false) foreach  (MeasurementMessageSender.send _)}
		MeasurementMessageSender.stop()
	}

	test("JMS message sending") {
		time {
			val it = dataGenerator.dailyDataIterator(20, realTime = false)
			MeasurementMessageSender.start()
			for (i <- 1 to 100000) {
				MeasurementMessageSender.send(it.next())
			}
			MeasurementMessageSender.sendStop()
			MeasurementMessageSender.stop()
		}
	}


}
