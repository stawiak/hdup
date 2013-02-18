package com.os.mql.model

/**
 * @author Vadim Bobrov
 */
object MQLTable {

	def apply(name: String): MQLTable = {
		name match {
			case "energy" =>
				MQLTableEnergy
			case "current" =>
				MQLTableCurrent
			case "vamps" =>
				MQLTableVamps
			case "interpolated" =>
				MQLTableInterpolated
			case "rollup" =>
				MQLTableRollup

		}
	}
}
sealed abstract class MQLTable
case object MQLTableEnergy extends MQLTable { override def toString: String = "energy" }
case object MQLTableCurrent extends MQLTable { override def toString: String = "current" }
case object MQLTableVamps extends MQLTable { override def toString: String = "vamps" }
case object MQLTableInterpolated extends MQLTable { override def toString: String = "interpolated" }
case object MQLTableRollup extends MQLTable { override def toString: String = "rollup" }





