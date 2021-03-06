package com.os.dao

import clwt.CLWTRowKeyUtils
import org.apache.hadoop.hbase.filter.{PrefixFilter, Filter}

/**
 * @author Vadim Bobrov
*/
object PrefixFilterFactory {

  /**
   * returns a filter that can be set on a scanner to limit to a certain
   * customer and location
   *
   * an alternative to spawning multiple concurrent region scans
   *
   * @param customer
   * @param location
   * @return
   */
  def createFilter(customer: String, location: String): Filter = {
    new PrefixFilter(CLWTRowKeyUtils.createRowKeyPrefix(customer, location))
  }

}
