package com.outsmart.dao

/**
 * @author Vadim Bobrov
*/
class ScannerServiceImpl extends ScannerService {
	//TODO consider apply
  def getScanner(): Scanner =  new ScannerImpl()
}
