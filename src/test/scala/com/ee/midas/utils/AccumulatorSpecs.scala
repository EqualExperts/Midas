package com.ee.midas.utils

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
object AccumulatorSpecs extends Specification{

    "Accumulator" should {
       "accumulate" in {
          val accumulate = Accumulator[Integer](Nil)
         accumulate(1)
         accumulate(2)
         accumulate(null) mustEqual(List(2,1))
       }
    }
}
