package com.outsmart

/**
 * @author Vadim Bobrov
*/
class ScanerServiceImpl extends ScanerService {
  def getScanner(): Scaner =  new ScanerImpl()
}
