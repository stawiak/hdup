package com.outsmart

import scala.actors.Futures._
import org.joda.time.DateTime
import actors.Future
import scala.collection.JavaConversions._


/**
 * @author Vadim Bobrov
*/
object Retriever {

  def main(args: Array[String]) {

    val start = System.currentTimeMillis()


    val grabber = new Grabber()

    Console.println((System.currentTimeMillis() - start) + " ms")


  }

}
