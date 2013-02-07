package com.os.mql

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

		}
	}
}
sealed abstract class MQLTable
case class MQLTableEnergy() extends MQLTable { override def toString: String = "energy" }
case class MQLTableCurrent() extends MQLTable { override def toString: String = "current" }
case class MQLTableVamps() extends MQLTable { override def toString: String = "vamps" }





