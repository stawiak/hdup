package com.os.util

/**
  * @author Vadim Bobrov
  */
trait TimeWindowMap[A, B] {

	 /** Appends a single element to this buffer. This operation takes constant time.
	   *
	   *  @param x  the element to append.
	   *  @return   this $coll.
	   */
	 def += (x: (A, B)): this.type

	 /** The length of the $coll.
	   *
	   *  $willNotTerminateInf
	   *
	   *  Note: the execution of `length` may take time proportial to the length of the sequence.
	   */
	 def size: Int

	 def sortWith(lt: (A, A) => Boolean): TimeWindowMap[A, B]

	 def span(p: ((A,B)) => Boolean): (TimeWindowMap[A, B], TimeWindowMap[A, B])

	/** Retrieves the value which is associated with the given key. This
	  *  method invokes the `default` method of the map if there is no mapping
	  *  from the given key to a value. Unless overridden, the `default` method throws a
	  *  `NoSuchElementException`.
	  *
	  *  @param  key the key
	  *  @return     the value associated with the given key, or the result of the
	  *              map's `default` method, if none exists.
	  */
	def apply(key: A): B

	/** Tests whether this map contains a binding for a key.
	  *
	  *  @param key the key
	  *  @return    `true` if there is a binding for `key` in this map, `false` otherwise.
	  */
	def contains(key: A): Boolean

	def foreach(f: ((A, B)) => Unit)

 }
