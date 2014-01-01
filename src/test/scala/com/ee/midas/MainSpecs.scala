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
         deltasURI mustEqual "deltas"
       }

      " runs on a given PORT and connects to default source and mongoPort" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--port","27040"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27040
        mongoHost mustEqual "localhost"
        mongoPort mustEqual 27017
        transformType mustEqual TransformType.EXPANSION
        deltasURI mustEqual "deltas"
      }

      " runs on default port and connects to given MONGOHOST on default mongoPort" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--source","192.168.1.44"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27020
        mongoHost mustEqual "192.168.1.44"
        mongoPort mustEqual 27017
        transformType mustEqual TransformType.EXPANSION
        deltasURI mustEqual "deltas"
      }

      " runs on default port and connects to default mongoHost on given MONGOPORT" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--mongoPort","27019"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27020
        mongoHost mustEqual "localhost"
        mongoPort mustEqual 27019
        transformType mustEqual TransformType.EXPANSION
        deltasURI mustEqual "deltas"
      }

      " runs on given PORT and connects to given MONGOHOST on default mongoPort" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--port","27040","--source","192.168.1.44"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27040
        mongoHost mustEqual "192.168.1.44"
        mongoPort mustEqual 27017
        transformType mustEqual TransformType.EXPANSION
        deltasURI mustEqual "deltas"
      }

      " runs on given PORT and connects to default mongoHost on given MONGOPORT" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--port","27040","--mongoPort","27019"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27040
        mongoHost mustEqual "localhost"
        mongoPort mustEqual 27019
        transformType mustEqual TransformType.EXPANSION
        deltasURI mustEqual "deltas"
      }

      " runs on default port and connects to given MONGOHOST on MONGOPORT" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--source","192.168.1.44","--mongoPort","27019"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27020
        mongoHost mustEqual "192.168.1.44"
        mongoPort mustEqual 27019
        transformType mustEqual TransformType.EXPANSION
        deltasURI mustEqual "deltas"
      }

      " runs in CONTRACTION mode" in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--port","27040","--source","192.168.1.44","--mongoPort","27019","--mode","CONTRACTION"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27040
        mongoHost mustEqual "192.168.1.44"
        mongoPort mustEqual 27019
        transformType mustEqual TransformType.CONTRACTION
        deltasURI mustEqual "deltas"
      }

      " uses the specified directory for picking up delta files " in {
        val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int,
        transformType: TransformType, deltasURI: String) = Main.processCLIparameters(Array("--port","27040",
          "--source","192.168.1.44","--mongoPort","27019","--mode","CONTRACTION","--deltasDir","src/main/resources/deltas"))

        midasHost mustEqual "localhost"
        midasPort mustEqual 27040
        mongoHost mustEqual "192.168.1.44"
        mongoPort mustEqual 27019
        transformType mustEqual TransformType.CONTRACTION
        deltasURI mustEqual "src/main/resources/deltas"
      }
    }
}
