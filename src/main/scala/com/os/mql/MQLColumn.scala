package com.os.mql

/**
 * @author Vadim Bobrov
 */
object MQLColumn {

	def apply(name: String): MQLColumn = {
		name match {
			case "*" =>
				MQLColumnAll()
			case "timestamp" =>
				MQLColumnTimestamp()
			case "value" =>
				MQLColumnValue()

		}
	}
}
sealed abstract class MQLColumn
case class MQLColumnAll() extends MQLColumn  { override def toString: String = "*" }
case class MQLColumnTimestamp() extends MQLColumn  { override def toString: String = "timestamp" }
case class MQLColumnValue() extends MQLColumn  { override def toString: String = "value" }
case class MQLColumnStringLiteral(s: String) extends MQLColumn  { override def toString: String = s }
