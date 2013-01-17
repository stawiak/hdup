package com.outsmart.dao

import actors.Futures._
import org.joda.time.DateTime
import com.outsmart.measurement.MeasuredValue
import com.outsmart.util.Loggable

/**
 * @author Vadim Bobrov
*/
class Grabber(scanner : Scanner) extends Loggable{

	//TODO consider apply
  /**
   * Given the set of start and end periods retrieve the data
   * for a customer, location and wireid
   * @param periods array of start and end times
   * @return
   */
  def grab(customer : String, location : String, wireid : String, periods : List[(String, String)]) : List[MeasuredValue] = {
    periods map (period => future { runScan(customer, location, wireid, period) }) flatMap (_())
  }

  private def runScan(customer : String, location : String, wireid : String, arg : (String, String)) : List[MeasuredValue] = {
    debug("starting scanner in thread " + Thread.currentThread().getId + " for " + customer + ", " + location + ", " + wireid + " from " + arg._1 + " to " + arg._2)
    scanner.scan(customer, location, wireid, new DateTime(arg._1), new DateTime(arg._2))
  }
}
