package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.measurement.TimedValue
import com.os.interpolation.NQueueImpl


/**
 * @author Vadim Bobrov
 */
class NQueueImplTest extends FlatSpec with ShouldMatchers {

	val tv1 = new TimedValue(111, 0.1)
	val tv2 = new TimedValue(222, 0.2)
	val tv3 = new TimedValue(333, 0.3)
	val tv4 = new TimedValue(444, 0.4)
	val tv5 = new TimedValue(555, 0.5)

	val sameAs3 = new TimedValue(333, 0.3)

	"NQueue" should "correctly add one element" in {
		val queue = new NQueueImpl
		queue.offer(tv1)
		queue.content should have size (1)
		queue.content.apply(0) should be (tv1)
		queue.get(3) should be (tv1)
		queue.isFull should be (false)
	}

	it should "correctly add two elements" in {
		val queue = new NQueueImpl
		queue.offer(tv1)
		queue.offer(tv2)
		queue.content should have size (2)
		queue.content.apply(0) should be (tv1)
		queue.content.apply(1) should be (tv2)
		queue.get(3) should be (tv2)
		queue.get(2) should be (tv1)
		queue.isFull should be (false)
	}

	it should "correctly add three elements" in {
		val queue = new NQueueImpl
		queue.offer(tv1)
		queue.offer(tv2)
		queue.offer(tv3)
		queue.content should have size (3)
		queue.content.apply(0) should be (tv1)
		queue.content.apply(1) should be (tv2)
		queue.content.apply(2) should be (tv3)
		queue.get(3) should be (tv3)
		queue.get(2) should be (tv2)
		queue.get(1) should be (tv1)
		queue.isFull should be (false)
	}

	it should "correctly add four elements" in {
		val queue = new NQueueImpl
		queue.offer(tv1)
		queue.offer(tv2)
		queue.offer(tv3)
		queue.offer(tv4)
		queue.content should have size (4)
		queue.content.apply(0) should be (tv1)
		queue.content.apply(1) should be (tv2)
		queue.content.apply(2) should be (tv3)
		queue.content.apply(3) should be (tv4)
		queue.get(3) should be (tv4)
		queue.get(2) should be (tv3)
		queue.get(1) should be (tv2)
		queue.get(0) should be (tv1)
		queue.isFull should be (true)
	}

	it should "reject unsorted input" in {
		val queue = new NQueueImpl
		queue.offer(tv2)
		evaluating(queue.offer(tv1)) should produce [IllegalArgumentException]
	}

	it should "ignore nulls" in {
		val queue = new NQueueImpl
		queue.offer(tv1)
		queue.offer(null)
		queue.content should have size (1)
		queue.content.apply(0) should be (tv1)
		queue.get(3) should be (tv1)
	}

	it should "ignore duplicates" in {
		val queue = new NQueueImpl
		queue.offer(tv1)
		queue.offer(tv1)
		queue.content should have size (1)
		queue.content.apply(0) should be (tv1)
		queue.get(3) should be (tv1)
	}

}
