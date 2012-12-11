package com.outsmart

/**
  * @author Vadim Bobrov
 */
class TestScannerServiceImpl extends ScanerService {
   def getScanner(): Scaner =  new TestScanerImpl()
 }
