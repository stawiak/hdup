package com.os.unit

import org.scalatest.FlatSpec
import com.os.measurement._
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
 */
class MeasurementTest extends FlatSpec with ShouldMatchers {

	"Different measurements" should "be equal if only value is different" in {
		val a = new EnergyMeasurement("", "", "", 111, 111)
		val b = new EnergyMeasurement("", "", "", 111, 222)
		a should be (b)
	}

	"Different measurements" should "not be equal if timestamp is different" in {
		val a = new EnergyMeasurement("", "", "", 112, 111)
		val b = new EnergyMeasurement("", "", "", 111, 111)
		a should not be (b)
	}

	"Different measurements" should "not be equal if customer is different" in {
		val a = new EnergyMeasurement("a", "", "", 111, 111)
		val b = new EnergyMeasurement("b", "", "", 111, 111)
		a should not be (b)
	}

	"Different measurements" should "not be equal if location is different" in {
		val a = new EnergyMeasurement("", "c", "", 111, 111)
		val b = new EnergyMeasurement("", "1", "", 111, 111)
		a should not be (b)
	}

	"Different measurements" should "not be equal if wireid is different" in {
		val a = new EnergyMeasurement("", "", "4", 111, 111)
		val b = new EnergyMeasurement("", "", "", 111, 111)
		a should not be (b)
	}



	"Measurements hash code" should "be equal if values are equal" in {
		val a = new EnergyMeasurement("", "", "", 111, 111)
		val b = new EnergyMeasurement("", "", "", 111, 222)
		a.hashCode() should be (b.hashCode())
	}

	"Measurements compare" should "return negative if timestamp is less" in {
		val a = new EnergyMeasurement("", "", "", 111, 111)
		val b = new EnergyMeasurement("", "", "", 112, 111)
		a.compareTo(b) should be < (0)
	}

	"Measurements compare" should "return negative if customer is less" in {
		val a = new EnergyMeasurement("a", "", "", 111, 111)
		val b = new EnergyMeasurement("b", "", "", 111, 111)
		a.compareTo(b) should be < (0)
	}

	"Measurements compare" should "return negative if location is less" in {
		val a = new EnergyMeasurement("", "c", "", 111, 111)
		val b = new EnergyMeasurement("", "d", "", 111, 111)
		a.compareTo(b) should be < (0)
	}

	"Measurements compare" should "return negative if wireid is less" in {
		val a = new EnergyMeasurement("", "", "e", 111, 111)
		val b = new EnergyMeasurement("", "", "f", 111, 111)
		a.compareTo(b) should be < (0)
	}



	"Measurements compare" should "return positive if timestamp is more" in {
		val a = new EnergyMeasurement("", "", "", 112, 111)
		val b = new EnergyMeasurement("", "", "", 111, 111)
		a.compareTo(b) should be > (0)
	}

	"Measurements compare" should "return negative if customer is more" in {
		val a = new EnergyMeasurement("b", "", "", 111, 111)
		val b = new EnergyMeasurement("a", "", "", 111, 111)
		a.compareTo(b) should be > (0)
	}

	"Measurements compare" should "return negative if location is more" in {
		val a = new EnergyMeasurement("", "d", "", 111, 111)
		val b = new EnergyMeasurement("", "c", "", 111, 111)
		a.compareTo(b) should be > (0)
	}

	"Measurements compare" should "return negative if wireid is more" in {
		val a = new EnergyMeasurement("", "", "f", 111, 111)
		val b = new EnergyMeasurement("", "", "e", 111, 111)
		a.compareTo(b) should be > (0)
	}


	"Measurements compare" should "return 0 if values are equal" in {
		val a = new EnergyMeasurement("", "", "", 111, 111)
		val b = new EnergyMeasurement("", "", "", 111, 222)
		a.compareTo(b) should be (0)
	}

  "Measurements" should "pattern match correctly" in {
    val a = new EnergyMeasurement("", "", "", 111, 111)
    val b = new EnergyMeasurement("", "", "", 111, 222) with Rollup
    matchResult(a) should be ("energy")
    matchResult(b) should be ("rollup")
    matchMeasurementResult(a) should be ("measurement")
  }

  private def matchResult(msmt: Measurement): String =
      msmt match  {
        case msmt: Interpolated => "interpolated"
        case msmt : Rollup => "rollup"
        case msmt: EnergyMeasurement => "energy"
        case msmt: CurrentMeasurement => "current"
        case msmt: VampsMeasurement => "vamps"
      }

  private def matchMeasurementResult(msmt: Measurement): String =
    msmt match  {
      case msmt: Measurement => "measurement"
    }

}
