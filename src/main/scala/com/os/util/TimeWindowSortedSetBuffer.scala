package com.os.util

import collection.mutable

/**
 * @author Vadim Bobrov
 */
class TimeWindowSortedSetBuffer[A <% Ordered[A]]() extends TimeWindow[A] {

	// from method returns all elements greater than or equal to a
	// starting element in the set’s ordering. The result of calls
	// to both methods is again a sorted set.

	var set: mutable.SortedSet[A] = mutable.TreeSet.empty[A]

	private def this(fromSet: mutable.SortedSet[A]) = {
		this()
		set = fromSet
	}

	/** Appends a single element to this buffer. This operation takes constant time.
	  *
	  *  @param x  the element to append.
	  *  @return   this $coll.
	  */
	def += (x: A): this.type = {
		set add x
		this
	}

	/**
	  * The length of the $coll.
	  */
	def size: Int = set.size

	def sortWith(lt: (A, A) => Boolean): TimeWindow[A] = this


	def span(p: A => Boolean): (TimeWindow[A], TimeWindow[A]) = (new TimeWindowSortedSetBuffer(set.span(p)._1), new TimeWindowSortedSetBuffer(set.span(p)._2))


	def foreach(f: A => Unit) { set.foreach(f) }

}
