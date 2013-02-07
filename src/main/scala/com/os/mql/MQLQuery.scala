package com.os.mql

/**
 * @author Vadim Bobrov
 */
class MQLFrom(val table: MQLTable) {
	override def toString: String = "from\n\t" + table
}


class MQLSelect(val columns: List[MQLColumn]) {
	override def toString: String = "select\n\t" + columns.mkString(",")
}

class MQLQuery(val select: MQLSelect, val from: MQLFrom) {
	override def toString: String = "\n" + select + "\n" + from
}
