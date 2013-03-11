package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import akka.actor._
import akka.testkit.TestKit

/**
 * @author Vadim Bobrov
 */
class TestKitTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with BeforeAndAfterAll {

	def this() = this(ActorSystem())

	// OneInstancePerTest makes this constructor to be executed twice
	// causing all sorts of errors when starting ActorSystem. Avoid!
	println("startup")

	override def afterAll() {
		println("shutdown")
		system.shutdown()
	}

	"testkit" should "behave as expected 1" in {
		println("test1")
	}

	"testkit" should "behave as expected 2" in {
		println("test2")
	}

}
