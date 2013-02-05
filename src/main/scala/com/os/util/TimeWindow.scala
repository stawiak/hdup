package com.os.util

/**
 * @author Vadim Bobrov
 */
trait TimeWindow[A] {

	/** Appends a single element to this buffer. This operation takes constant time.
	  *
	  *  @param x  the element to append.
	  *  @return   this $coll.
	  */
	def += (x: A): this.type

	/** The length of the $coll.
	  *
	  *  $willNotTerminateInf
	  *
	  *  Note: the execution of `length` may take time proportial to the length of the sequence.
	  */
	def size: Int

	def sortWith(lt: (A, A) => Boolean): TimeWindow[A]

	def span(p: A => Boolean): (TimeWindow[A], TimeWindow[A])

	def foreach(f: A => Unit)

}
