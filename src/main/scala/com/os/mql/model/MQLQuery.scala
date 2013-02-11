package com.os.mql.model


/**
 * @author Vadim Bobrov
 */

case class InvalidMQLException(msg: String) extends Exception(msg)

case class MQLSelect(columns: Traversable[MQLColumn]) {
	override def toString: String = "select\n\t" + columns.mkString(",")
}

case class MQLFrom(table: MQLTable) {
	override def toString: String = "from\n\t" + table
}

case class MQLWhere(conds: Traversable[MQLCondition]) {

	// comparison by customer and location are required
	if (conds filter { isCustomerCondition(_) } isEmpty )
		throw new InvalidMQLException("Customer must be specified")

	if (conds filter { isLocationCondition(_) } isEmpty )
		throw new InvalidMQLException("Location must be specified")

	private def isCustomerCondition(cond: MQLCondition): Boolean = {
		cond.isInstanceOf[MQLComparisonStringCondition] && cond.asInstanceOf[MQLComparisonStringCondition].col.isInstanceOf[MQLColumnCustomer]
	}

	private def isLocationCondition(cond: MQLCondition): Boolean = {
		cond.isInstanceOf[MQLComparisonStringCondition] && cond.asInstanceOf[MQLComparisonStringCondition].col.isInstanceOf[MQLColumnLocation]
	}

	override def toString: String = "where\n\t" + conds.mkString("\nand\n")
}

case class MQLQuery(select: MQLSelect, from: MQLFrom, where: Option[MQLWhere]) {
	override def toString: String = select + "\n" + from + (if(where.isDefined) "\n" + where.get else "")
}

case class MQLUnion(queries: Traversable[MQLQuery]) {
	override def toString: String = queries.mkString("\nunion\n")
}

