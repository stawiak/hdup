package com.os.mql.model


/**
 * @author Vadim Bobrov
 */
case class MQLSelect(val columns: List[MQLColumn]) {
	override def toString: String = "select\n\t" + columns.mkString(",")
}

case class MQLFrom(val table: MQLTable) {
	override def toString: String = "from\n\t" + table
}

case class MQLWhere(val cond: MQLCondition) {
	override def toString: String = "where\n\t" + cond
}

case class MQLQuery(val select: MQLSelect, val from: MQLFrom, val where: Option[MQLWhere]) {
	override def toString: String = select + "\n" + from + (if(where.isDefined) "\n" + where.get else "")
}

case class MQLUnion(val queries: List[MQLQuery]) {
	override def toString: String = queries.mkString("\nunion\n")
}

