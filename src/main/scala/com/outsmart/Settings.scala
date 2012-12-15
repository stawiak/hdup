package com.outsmart

/**
 * @author Vadim Bobrov
*/
object Settings {

  val TableName = "msmt"
  val ColumnFamilyName = "d"    // stands for data
  val QualifierName = "p"        // stands for power
  val Host = "10.0.0.158"
  val BatchSize = 1000          // writer batch size

}
