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
case class MQLColumnAll() extends MQLColumn
case class MQLColumnTimestamp() extends MQLColumn
case class MQLColumnValue() extends MQLColumn
