package com.outsmart

/**
  * @author Vadim Bobrov
 */
class TestScannerServiceImpl extends ScannerService {
   def getScanner(): Scanner =  new TestScannerImpl()
 }
