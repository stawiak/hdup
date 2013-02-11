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

case class MQLQuery(select: MQLSelect, from: MQLFrom, where: Option[MQLWhere]) {

	if (where.isDefined) {
		if (!from.table.isInstanceOf[MQLTableRollup] && where.get.wireidCondition.isEmpty)
			throw new InvalidMQLException("Wire id must be specified if not querying rollups")

		if (from.table.isInstanceOf[MQLTableRollup] && where.get.wireidCondition.isDefined)
			throw new InvalidMQLException("Wire id may not be specified if querying rollups")
	}

	override def toString: String = select + "\n" + from + (if(where.isDefined) "\n" + where.get else "")
}

case class MQLUnion(queries: Traversable[MQLQuery]) {
	override def toString: String = queries.mkString("\nunion\n")
}


case class MQLWhere(conds: Traversable[MQLCondition]) {

	val customerCondition: Option[MQLCustomerCondition] = conds collect {case x: MQLCustomerCondition => x} headOption
	val locationCondition: Option[MQLLocationCondition] = conds collect {case x: MQLLocationCondition => x} headOption
	val wireidCondition: Option[MQLWireIdCondition] = conds collect {case x: MQLWireIdCondition => x} headOption

	// comparison by customer and location are required
	if (customerCondition.isEmpty)
		throw new InvalidMQLException("Customer must be specified")

	if (locationCondition.isEmpty)
		throw new InvalidMQLException("Location must be specified")

	override def toString: String = "where\n\t" + conds.mkString("\nand\n")
}

