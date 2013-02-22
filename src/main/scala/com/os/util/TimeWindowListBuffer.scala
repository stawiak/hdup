package com.os.util


/**
 * @author Vadim Bobrov
 */
class TimeWindowListBuffer[A] extends TimeWindow[A] {

	var list: List[A] = List.empty[A]

	private def this(fromList: List[A]) = {
		this()
		list = fromList
	}

	/** Appends a single element to this buffer. This operation takes constant time.
	  *
	  *  @param x  the element to append.
	  *  @return   this $coll.
	  */
	def += (x: A): this.type = {
		list ::= x
		this
	}

	/**
	  * The length of the $coll.
	  */
	def size: Int = list.size

	def sortWith(lt: (A, A) => Boolean): TimeWindow[A] = {
		list.sortWith(lt)
		this
	}

	def span(p: A => Boolean): (TimeWindow[A], TimeWindow[A]) = {
		throw new NotImplementedError("list must be sorted as span will stop at first element the condition returns true")
	}


	def foreach(f: A => Unit) { list.foreach(f) }

}
