package com.os.mql

/**
 * @author Vadim Bobrov
 */
case class MQLCondition(col: MQLColumn, cmp: String, value: Double) {
	override def toString: String = col + " " + cmp + " " + value
}
