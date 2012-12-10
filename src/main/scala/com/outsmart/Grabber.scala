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
   * @param periods array of start and end times
   * @return
   */
  def grab(customer : String, location : String, wireid : String, periods : Array[(String, String)]) : List[Long] = {
    var futures = List[Future[Array[Long]]]()

    // dispatch scanners in parallel
    periods foreach (arg => {
      Console.println("starting scanner")

      val f = future {
        val res : Array[Long] = scannerService.getScanner().scan(customer, location, wireid, new DateTime(arg._1), new DateTime(arg._2))
        res
      }

      futures = futures ::: List(f)
    })

    var results = List[Long]()

    futures foreach ( f => {
      f() foreach (l => results = results ::: List(l))
    })

    results
  }

}
