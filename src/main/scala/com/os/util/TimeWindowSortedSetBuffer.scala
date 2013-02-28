package com.os.util

import collection.mutable


/**
 * @author Vadim Bobrov
 */
class TimeWindowSortedSetBuffer[A <% Ordered[A]] extends TimeWindow[A] with Loggable {

	val set: mutable.SortedSet[A] = mutable.TreeSet.empty[A]

	private def this(fromSet: mutable.SortedSet[A]) = {
		this()
		set ++= fromSet
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


	/**
	 * Splits this coll into two by predicate
	 * @param p	  predicate to split by
	 * @return    elems that satisfy/not satisfy predicate
	 */
	def span(p: A => Boolean): (TimeWindow[A], TimeWindow[A]) = {
		try {
			val(left, right) = set span p
			(new TimeWindowSortedSetBuffer[A](left), new TimeWindowSortedSetBuffer[A](right))
		} catch {
			case e: Exception =>
				log.debug("null Exception")
				log.debug("p: " + p)
				log.debug("s: " + set)
				throw e
		}
	}


	def foreach(f: A => Unit) { set.foreach(f) }

}
