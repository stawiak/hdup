package com.outsmart

import dao.{ScannerService, Scanner}

/**
 * @author Vadim Bobrov
 */
class TestScannerServiceImpl extends ScannerService {
	def getScanner(): Scanner =  new TestScanerImpl()
}
