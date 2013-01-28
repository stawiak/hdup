package com.os.util

import collection.JavaConversions._

/**
 * @author Vadim Bobrov
 */
class CollectionConverter[T](val col: List[T]) {

	def toList : java.util.List[T] = {
		col
	}
}
