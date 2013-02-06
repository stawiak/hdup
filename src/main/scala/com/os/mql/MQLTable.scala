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
case class MQLTableEnergy() extends MQLTable
case class MQLTableCurrent() extends MQLTable
case class MQLTableVamps() extends MQLTable





