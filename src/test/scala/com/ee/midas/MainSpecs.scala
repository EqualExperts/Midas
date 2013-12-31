package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.TransformType

@RunWith(classOf[JUnitRunner])
class MainSpecs extends Specification{
    "Midas" should {
       "run with default values" in {
         val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
              transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array())

         midasHost mustEqual "localhost"
         midasPort mustEqual 27020
         mongoHost mustEqual "localhost"
         mongoPort mustEqual 27017
         transformType mustEqual TransformType.EXPANSION
         deltasURI mustEqual "deltas/"
       }

    }
}
