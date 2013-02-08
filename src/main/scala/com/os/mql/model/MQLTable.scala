package com.os.mql.model

/**
 * @author Vadim Bobrov
 */
object MQLTable {

	def apply(name: String): MQLTable = {
		name match {
			case "energy" =>
				MQLTableEnergy()
			case "current" =>
				MQLTableCurrent()
			case "vamps" =>
				MQLTableVamps()
			case "interpolated" =>
				MQLTableInterpolated()
			case "rollup" =>
				MQLTableRollup()

		}
	}
}
sealed abstract class MQLTable
case class MQLTableEnergy() extends MQLTable { override def toString: String = "energy" }
case class MQLTableCurrent() extends MQLTable { override def toString: String = "current" }
case class MQLTableVamps() extends MQLTable { override def toString: String = "vamps" }
case class MQLTableInterpolated() extends MQLTable { override def toString: String = "interpolated" }
case class MQLTableRollup() extends MQLTable { override def toString: String = "rollup" }





