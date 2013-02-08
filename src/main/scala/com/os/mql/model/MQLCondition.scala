package com.os.mql.model


/**
 * @author Vadim Bobrov
 */
abstract class MQLCondition

case class MQLComparisonNumberCondition(col: MQLColumn, cmp: String, value: Double) extends MQLCondition {
	override def toString: String = col + " " + cmp + " " + value
}

case class MQLComparisonStringCondition(col: MQLColumn, cmp: String, value: String) extends MQLCondition {
	override def toString: String = col + " " + cmp + " " + value
}

case class MQLBetweenCondition(col: MQLColumn, startValue: Double, endValue: Double) extends MQLCondition {
	override def toString: String = col + " between " + startValue + " and " + endValue
}

