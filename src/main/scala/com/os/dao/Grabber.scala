package com.os.dao

import org.joda.time.{Interval}
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
  def grab(customer : String, location : String, wireid : String, periods : Array[Interval]) : Array[MeasuredValue] = {
    periods map (period => Future { runScan(customer, location, wireid, period) }) flatMap (Await.result(_, Duration.Inf))
  }

  private def runScan(customer : String, location : String, wireid : String, period : Interval) : Array[MeasuredValue] = {
    scanner.scan(customer, location, wireid, period)
  }
}
