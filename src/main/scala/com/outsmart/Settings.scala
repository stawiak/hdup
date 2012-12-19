package com.outsmart

/**
 * @author Vadim Bobrov
*/
object Settings {

  val TableName = "msmt"
  val ColumnFamilyName = "d"      // stands for data
  val EnergyQualifierName = "e"   // stands for energy
  val CurrentQualifierName = "c"  // stands for current
  val VampireQualifierName = "v"  // stands for volt-amp-reactive

  val BatchSize = 1000            // writer batch size - can be lost
  val TablePoolSize = 100

  val Host = "192.168.152.128"
  //val Host = "10.0.0.158"


}
