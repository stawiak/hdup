package com.os.util

import collection.SortedMap


/**
 * @author Vadim Bobrov
 */
class TimeWindowSortedMap[A <% Ordered[A], B] extends TimeWindowMap[A, B] {

	var map = SortedMap.empty[A, B]

	private def this(fromMap: SortedMap[A, B]) = {
		this()
		map ++= fromMap
	}


	/** Appends a single element to this buffer. This operation takes constant time.
	  *
	  *  @param x  the element to append.
	  *  @return   this $coll.
	  */
	def += (x: (A, B)): this.type = {
		map += x
		this
	}



	/**
	  * The length of the $coll.
	  */
	def size: Int = map.size

	def sortWith(lt: (A, A) => Boolean): TimeWindowMap[A, B] = this


	/**
	 * Splits this coll into two by predicate
	 * @param p	  predicate to split by
	 * @return    elems that satisfy/not satisfy predicate
	 */
	def span(p: ((A,B)) => Boolean): (TimeWindowMap[A, B], TimeWindowMap[A, B]) = {
		val(left, right) = map span p
		(new TimeWindowSortedMap[A, B](left), new TimeWindowSortedMap[A, B](right))
	}

	//TODO refactor common functions into a subclass
	/** Retrieves the value which is associated with the given key. This
	  *  method invokes the `default` method of the map if there is no mapping
	  *  from the given key to a value. Unless overridden, the `default` method throws a
	  *  `NoSuchElementException`.
	  *
	  *  @param  key the key
	  *  @return     the value associated with the given key, or the result of the
	  *              map's `default` method, if none exists.
	  */
	def apply(key: A): B = map.get(key) match {
		case None => default(key)
		case Some(value) => value
	}

	/** Defines the default value computation for the map,
	  *  returned when a key is not found
	  *  The method implemented here throws an exception,
	  *  but it might be overridden in subclasses.
	  *
	  *  @param key the given key value for which a binding is missing.
	  *  @throws `NoSuchElementException`
	  */
	def default(key: A): B =
		throw new NoSuchElementException("key not found: " + key)


	/** Tests whether this map contains a binding for a key.
	  *
	  *  @param key the key
	  *  @return    `true` if there is a binding for `key` in this map, `false` otherwise.
	  */
	def contains(key: A): Boolean = map.get(key).isDefined

	def foreach(f: ((A, B)) => Unit) { map.foreach(f) }

}
