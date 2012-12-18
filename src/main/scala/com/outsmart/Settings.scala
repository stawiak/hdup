package com.outsmart

/**
 * @author Vadim Bobrov
*/
object Settings {

  val TableName = "msmt"
  val ColumnFamilyName = "d"     // stands for data
  val QualifierName = "p"        // stands for power

  val BatchSize = 1000           // writer batch size - can be lost
  val TablePoolSize = 100

  val Host = "192.168.152.128"
  //val Host = "10.0.0.158"


}
