package com.outsmart

import actors.Future
import actors.Futures._
import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
class Grabber(scannerService : ScannerService) {

  //TODO: should return timestamp as well

  /**
   * Given the set of start and end periods retrieve the data
   * for a customer, location and wireid
   * @param periods array of start and end times
   * @return
   */
  def grab(customer : String, location : String, wireid : String, periods : Array[(String, String)]) : List[Measurement] = {
    var futures = List[Future[Array[Measurement]]]()

    // dispatch scanners in parallel
    periods foreach (arg => {
      println("starting scanner")

      val f = future {
        val res : Array[Measurement] = scannerService.getScanner().scan(customer, location, wireid, new DateTime(arg._1), new DateTime(arg._2))
        res
      }

      futures = f::futures
    })

    var results = List[Measurement]()

    futures foreach ( f => {
      f() foreach (l => results = l::results)
    })

    results
  }

}
