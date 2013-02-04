package com.os.util


/**
 * @author Vadim Bobrov
 */
abstract class TimeWindowBuffer[A] extends TimeWindow[A]{

	/** Appends a single element to this buffer. This operation takes constant time.
	  *
	  *  @param x  the element to append.
	  *  @return   this $coll.
	  */
	def += (x: A): this.type = {
		//TODO: add element
		this
	}
}
