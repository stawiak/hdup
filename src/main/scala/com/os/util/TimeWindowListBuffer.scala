package com.os.util


/**
 * @author Vadim Bobrov
 */
class TimeWindowListBuffer[A](var list: List[A] = Nil) extends TimeWindow[A] {

	//TODO: non-default constructor should be private

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
	def length: Int = list.length

	def sortWith(lt: (A, A) => Boolean): TimeWindow[A] = {
		list.sortWith(lt)
		this
	}

	def span(p: A => Boolean): (TimeWindow[A], TimeWindow[A]) = (new TimeWindowListBuffer(list.span(p)._1), new TimeWindowListBuffer(list.span(p)._2))


	def foreach(f: A => Unit) { list.foreach(f) }

}
