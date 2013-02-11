package com.os.mql.model


/**
 * @author Vadim Bobrov
 */

case class UnsupportedConditionException(msg: String) extends Exception(msg)
object MQLCondition {

	def apply(col: MQLColumn, cmp: String, value: String) = {
		(col,cmp) match {
			case (MQLColumnCustomer(), "=") =>
				MQLCustomerCondition(value)

			case (MQLColumnLocation(), "=") =>
				MQLLocationCondition(value)

			case (MQLColumnWireId(), "=") =>
				MQLWireIdCondition(value)

			case _ =>
				throw new UnsupportedConditionException("unsupported condition in MQL")
		}
	}

	def apply(col: MQLColumn, cmp: String, value: Double) = {
		col match {
			case MQLColumnValue() =>
				MQLValueCondition(cmp, value)

			case _ =>
				throw new UnsupportedConditionException("unsupported condition in MQL")
		}
	}


	def apply(col: MQLColumn, startValue: Double, endValue: Double) = {
		col match {
			case MQLColumnTimestamp() =>
				MQLTimeRangeCondition(startValue, endValue)

			case _ =>
				throw new UnsupportedConditionException("unsupported condition in MQL")
		}
	}


}

abstract class MQLCondition

abstract class MQLComparisonNumberCondition(val col: MQLColumn, val cmp: String, val value: Double) extends MQLCondition {
	override def toString: String = col + " " + cmp + " " + value
}

abstract class MQLComparisonStringCondition(val col: MQLColumn, val cmp: String, val value: String) extends MQLCondition {
	override def toString: String = col + " " + cmp + " " + value
}

abstract class MQLBetweenCondition(val col: MQLColumn, val startValue: Double, val endValue: Double) extends MQLCondition {
	override def toString: String = col + " between " + startValue + " and " + endValue
}

// the only currently implemented conditions
case class MQLCustomerCondition(override val value: String) extends MQLComparisonStringCondition(MQLColumnCustomer(), "=", value)
case class MQLLocationCondition(override val value: String) extends MQLComparisonStringCondition(MQLColumnLocation(), "=", value)
case class MQLWireIdCondition(override val value: String) extends MQLComparisonStringCondition(MQLColumnWireId(), "=", value)

case class MQLTimeRangeCondition(override val startValue: Double, override val endValue: Double) extends MQLBetweenCondition(MQLColumnTimestamp(), startValue, endValue)
case class MQLValueCondition(override val cmp: String, override val value: Double) extends MQLComparisonNumberCondition(MQLColumnValue(), cmp, value)
