package com.ee.midas.utils

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AccumulatorSpecs extends Specification {

    "Accumulator" should {

      "accumulate values " in {
        //Given
        val accumulate = Accumulator[Integer]

        //When
        accumulate(1)
        accumulate(2)

        //Then
        accumulate(null) mustEqual(List(2,1))
      }

      "Ignore Null values" in {
        //Given
        val accumulate = Accumulator[Integer]

        //When
        accumulate(null)
        val list = accumulate(1)

        //Then
        list mustEqual(List(1))
      }

      "Ignore Nil values" in {
        //Given
        val accumulate = Accumulator[Object]

        //When
        accumulate(Nil)
        val list = accumulate(new Integer(1))

        //Then
        list mustEqual(List(1))
      }
    }
}
