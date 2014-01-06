package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.TransformType

@RunWith(classOf[JUnitRunner])
class CLIParserSpecs extends Specification{
    "Midas" should {
       "run with default values" in {
         val config = CLIParser.parse(Array())

         config.midasHost mustEqual "localhost"
         config.midasPort mustEqual 27020
         config.mongoHost mustEqual "localhost"
         config.mongoPort mustEqual 27017
         config.mode mustEqual TransformType.EXPANSION
         config.deltasDir mustEqual "deltas"
       }

      " runs on a given PORT and connects to default source and mongoPort" in {
        val config = CLIParser.parse(Array("--port","27040"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27040
        config.mongoHost mustEqual "localhost"
        config.mongoPort mustEqual 27017
        config.mode mustEqual TransformType.EXPANSION
        config.deltasDir mustEqual "deltas"
      }

      " runs on default port and connects to given MONGOHOST on default mongoPort" in {
        val config = CLIParser.parse(Array("--source","192.168.1.44"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27020
        config.mongoHost mustEqual "192.168.1.44"
        config.mongoPort mustEqual 27017
        config.mode mustEqual TransformType.EXPANSION
        config.deltasDir mustEqual "deltas"
      }

      " runs on default port and connects to default mongoHost on given MONGOPORT" in {
        val config = CLIParser.parse(Array("--mongoPort","27019"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27020
        config.mongoHost mustEqual "localhost"
        config.mongoPort mustEqual 27019
        config.mode mustEqual TransformType.EXPANSION
        config.deltasDir mustEqual "deltas"
      }

      " runs on given PORT and connects to given MONGOHOST on default mongoPort" in {
        val config = CLIParser.parse(Array("--port","27040","--source","192.168.1.44"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27040
        config.mongoHost mustEqual "192.168.1.44"
        config.mongoPort mustEqual 27017
        config.mode mustEqual TransformType.EXPANSION
        config.deltasDir mustEqual "deltas"
      }

      " runs on given PORT and connects to default mongoHost on given MONGOPORT" in {
        val config = CLIParser.parse(Array("--port","27040","--mongoPort","27019"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27040
        config.mongoHost mustEqual "localhost"
        config.mongoPort mustEqual 27019
        config.mode mustEqual TransformType.EXPANSION
        config.deltasDir mustEqual "deltas"
      }

      " runs on default port and connects to given MONGOHOST on MONGOPORT" in {
        val config = CLIParser.parse(Array("--source","192.168.1.44","--mongoPort","27019"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27020
        config.mongoHost mustEqual "192.168.1.44"
        config.mongoPort mustEqual 27019
        config.mode mustEqual TransformType.EXPANSION
        config.deltasDir mustEqual "deltas"
      }

      " runs in CONTRACTION mode" in {
        val config = CLIParser.parse(Array("--port","27040","--source","192.168.1.44","--mongoPort","27019","--mode","CONTRACTION"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27040
        config.mongoHost mustEqual "192.168.1.44"
        config.mongoPort mustEqual 27019
        config.mode mustEqual TransformType.CONTRACTION
        config.deltasDir mustEqual "deltas"
      }

      " uses the specified directory for picking up delta files " in {
        val config = CLIParser.parse(Array("--port","27040",
          "--source","192.168.1.44","--mongoPort","27019","--mode","CONTRACTION","--deltasDir","src/main/resources/deltas"))

        config.midasHost mustEqual "localhost"
        config.midasPort mustEqual 27040
        config.mongoHost mustEqual "192.168.1.44"
        config.mongoPort mustEqual 27019
        config.mode mustEqual TransformType.CONTRACTION
        config.deltasDir mustEqual "src/main/resources/deltas"
      }
    }
}
