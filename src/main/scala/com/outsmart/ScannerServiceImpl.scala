package com.outsmart

/**
 * @author Vadim Bobrov
*/
class ScannerServiceImpl extends ScannerService {
  def getScanner(): Scanner =  new ScannerImpl()
}
