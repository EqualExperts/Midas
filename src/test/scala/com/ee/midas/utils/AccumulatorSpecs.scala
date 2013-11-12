package com.ee.midas.utils

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
object AccumulatorSpecs extends Specification {

    "Accumulator" should {

      "accumulate values " in {
         val accumulate = Accumulator[Integer](Nil)
         accumulate(1)
         accumulate(2)
         accumulate(null) mustEqual(List(2,1))
      }

      "throw a Exception when intialized with null " in {
         val accumulate = Accumulator[Object](null)
         accumulate(new Integer(1)) must throwA[NullPointerException]
      }

      "Ignore Null values" in {
        val accumulate = Accumulator[Integer](Nil)
        accumulate(null)
        val list = accumulate(1)
        list mustEqual(List(1))
      }

      "Ignore Nil values" in {
        val accumulate = Accumulator[Object](Nil)
        accumulate(Nil)
        val list = accumulate(new Integer(1))
        list mustEqual(List(1))
      }
    }
}
