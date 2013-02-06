package com.os.mql

/**
 * @author Vadim Bobrov
 */
class MQLSelect(val columns: List[MQLColumn]) {
	override def toString: String = "select " + columns.mkString
}

class MQLQuery(val select: MQLSelect) {
	override def toString: String = select.toString
}
