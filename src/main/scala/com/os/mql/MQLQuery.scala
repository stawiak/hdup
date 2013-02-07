package com.os.mql

/**
 * @author Vadim Bobrov
 */
class MQLSelect(val columns: List[MQLColumn]) {
	override def toString: String = "select\n\t" + columns.mkString(",")
}

class MQLFrom(val table: MQLTable) {
	override def toString: String = "from\n\t" + table
}

class MQLWhere(val cond: MQLCondition) {
	override def toString: String = "where\n\t" + cond
}

class MQLQuery(val select: MQLSelect, val from: MQLFrom, val where: Option[MQLWhere]) {
	override def toString: String = "\n" + select + "\n" + from + (if(where.isDefined) "\n" + where.get else "")
}
