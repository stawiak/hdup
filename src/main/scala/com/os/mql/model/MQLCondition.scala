package com.os.mql.model


/**
 * @author Vadim Bobrov
 */
abstract class MQLCondition

case class MQLComparisonCondition(col: MQLColumn, cmp: String, value: Double) extends MQLCondition {
	override def toString: String = col + " " + cmp + " " + value
}

case class MQLBetweenCondition(col: MQLColumn, startValue: Double, endValue: Double) extends MQLCondition {
	override def toString: String = col + " between " + startValue + " and " + endValue
}

