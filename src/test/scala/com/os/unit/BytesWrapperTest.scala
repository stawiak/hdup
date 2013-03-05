package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.util.BytesWrapper._
import com.os.util.BytesWrapper
import com.os.measurement.TimedValue
import com.os.interpolation.{NQueue, NQueueImpl}


/**
 * @author Vadim Bobrov
 */
class BytesWrapperTest extends FlatSpec with ShouldMatchers {

	val tv1 = new TimedValue(111, 0.1)
	val tv2 = new TimedValue(222, 0.2)
	val tv3 = new TimedValue(333, 0.3)
	val tv4 = new TimedValue(444, 0.4)

	"Byte wrapper" should "convert string" in {
		val res: String = new BytesWrapper << "hello byte"
		res should be ("hello byte")
	}

	it should "convert long" in {
		val res: Long = new BytesWrapper << 33L
		res should be (33L)
	}

	it should "convert double" in {
		val res: Double = new BytesWrapper << 33.3
		res should be (33.3)
	}

	it should "extract single timed value" in {
		val bytes = new BytesWrapper << tv1
		val tvs = bytes.extractTimedValues
		tvs should have size (1)
		tvs.head should be (tv1)
	}

	it should "extract two timed values" in {
		val bytes = new BytesWrapper << tv1 << tv2
		val tvs = bytes.extractTimedValues
		tvs should have size (2)
		tvs.toList(0) should be (tv1)
		tvs.toList(1) should be (tv2)
	}

	it should "extract four timed values" in {
		val bytes = new BytesWrapper << tv1 << tv2 << tv3 << tv4
		val tvs = bytes.extractTimedValues
		tvs should have size (4)
		tvs.toList(0) should be (tv1)
		tvs.toList(1) should be (tv2)
		tvs.toList(2) should be (tv3)
		tvs.toList(3) should be (tv4)
	}

	it should "extract no element NQueue" in {
		val queue = new NQueueImpl
		val bytes: Array[Byte] = queue
		val deserQueue: NQueue = bytes

		deserQueue.content.length should be (0)
		deserQueue.get(3) should be (null)
		deserQueue.get(2) should be (null)
		deserQueue.get(1) should be (null)
		deserQueue.get(0) should be (null)

		deserQueue.isFull should be (false)
	}

	it should "extract one element NQueue" in {
		val queue = new NQueueImpl
		queue offer tv1
		val bytes: Array[Byte] = queue
		val deserQueue: NQueue = bytes

		deserQueue.content.length should be (1)
		deserQueue.get(3) should be (tv1)
		deserQueue.get(2) should be (null)
		deserQueue.get(1) should be (null)
		deserQueue.get(0) should be (null)

		deserQueue.isFull should be (false)
	}

	it should "extract two element NQueue" in {
		val queue = new NQueueImpl
		queue offer tv1
		queue offer tv2
		val bytes: Array[Byte] = queue
		val deserQueue: NQueue = bytes

		deserQueue.content.length should be (2)
		deserQueue.get(3) should be (tv2)
		deserQueue.get(2) should be (tv1)
		deserQueue.get(1) should be (null)
		deserQueue.get(0) should be (null)

		deserQueue.isFull should be (false)
	}

	it should "extract three element NQueue" in {
		val queue = new NQueueImpl
		queue offer tv1
		queue offer tv2
		queue offer tv3
		val bytes: Array[Byte] = queue
		val deserQueue: NQueue = bytes

		deserQueue.content.length should be (3)
		deserQueue.get(3) should be (tv3)
		deserQueue.get(2) should be (tv2)
		deserQueue.get(1) should be (tv1)
		deserQueue.get(0) should be (null)

		deserQueue.isFull should be (false)
	}

	it should "extract four element NQueue" in {
		val queue = new NQueueImpl
		queue offer tv1
		queue offer tv2
		queue offer tv3
		queue offer tv4
		val bytes: Array[Byte] = queue
		val deserQueue: NQueue = bytes

		deserQueue.content.length should be (4)
		deserQueue.get(3) should be (tv4)
		deserQueue.get(2) should be (tv3)
		deserQueue.get(1) should be (tv2)
		deserQueue.get(0) should be (tv1)

		deserQueue.isFull should be (true)
	}

}
