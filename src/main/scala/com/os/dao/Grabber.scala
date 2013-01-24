package com.os.dao

import org.joda.time.DateTime
import com.os.measurement.MeasuredValue
import com.os.util.Loggable
import concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global
import concurrent.duration.Duration

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
    periods map (period => Future { runScan(customer, location, wireid, period) }) flatMap (Await.result(_, Duration.Inf))
  }

  private def runScan(customer : String, location : String, wireid : String, arg : (String, String)) : List[MeasuredValue] = {
    debug("starting scanner in thread " + Thread.currentThread().getId + " for " + customer + ", " + location + ", " + wireid + " from " + arg._1 + " to " + arg._2)
    scanner.scan(customer, location, wireid, new DateTime(arg._1), new DateTime(arg._2))
  }
}
