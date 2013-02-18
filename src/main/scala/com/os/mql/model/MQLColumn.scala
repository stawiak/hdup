package com.os.mql.model

/**
 * @author Vadim Bobrov
 */
object MQLColumn {

	def apply(name: String): MQLColumn = {
		name match {
			case "*" =>
				MQLColumnAll
			case "timestamp" =>
				MQLColumnTimestamp
			case "value" =>
				MQLColumnValue

		}
	}
}
sealed abstract class MQLColumn
case object MQLColumnAll extends MQLColumn  { override def toString: String = "*" }
case object MQLColumnTimestamp extends MQLColumn  { override def toString: String = "timestamp" }
case object MQLColumnValue extends MQLColumn  { override def toString: String = "value" }

case object MQLColumnCustomer extends MQLColumn  { override def toString: String = "customer" }
case object MQLColumnLocation extends MQLColumn  { override def toString: String = "location" }
case object MQLColumnWireId extends MQLColumn  { override def toString: String = "wireid" }

case class MQLColumnStringLiteral(value: String) extends MQLColumn  { override def toString: String = value }
