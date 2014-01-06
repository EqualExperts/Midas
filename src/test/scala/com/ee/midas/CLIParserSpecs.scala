package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.TransformType
import java.net.URI
import java.io.File

@RunWith(classOf[JUnitRunner])
class CLIParserSpecs extends Specification {
    val loader = this.getClass.getClassLoader
    val defaultDirURI : URI =  loader.getResource("deltas").toURI
    val userSuppliedURI : URI = new File("src/test/scala/com/ee/midas/newDeltas").toURI

    "Midas" should {
       "run with default values" in {
         CLIParser.parse(Array()) match {
           case Some(config) =>
             config.midasHost mustEqual "localhost"
             config.midasPort mustEqual 27020
             config.mongoHost mustEqual "localhost"
             config.mongoPort mustEqual 27017
             config.mode mustEqual TransformType.EXPANSION
             config.deltasDir mustEqual defaultDirURI
             success

           case None =>
              failure("Should have run with default Values")
         }
       }

      " runs on a given PORT and connects to default source and mongoPort" in {
        CLIParser.parse(Array("--port","27040")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run with given PORT while using defaults for source and mongoPort")

        }
      }

      " runs on default port and connects to given MONGOHOST on default mongoPort" in {
        CLIParser.parse(Array("--source","192.168.1.44")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run with given MONGOHOST while using defaults for port and mongoPort")

        }
      }

      " runs on default port and connects to default mongoHost on given MONGOPORT" in {
        CLIParser.parse(Array("--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.EXPANSION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run with given MONGOPORT while using defaults for port and mongoHost")

        }
      }

      " runs on given PORT and connects to given MONGOHOST on default mongoPort" in {
        CLIParser.parse(Array("--port","27040","--source","192.168.1.44")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run with given PORT and MONGOHOST while using defaults for mongoPort")

        }
      }

      " runs on given PORT and connects to default mongoHost on given MONGOPORT" in {
        CLIParser.parse(Array("--port","27040","--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.EXPANSION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run with given PORT and MONGOPORT while using defaults for mongoHost")

        }
      }

      " runs on default port and connects to given MONGOHOST on MONGOPORT" in {
        CLIParser.parse(Array("--source","192.168.1.44","--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.EXPANSION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run with given MONGOHOST and MONGOPORT while using defaults for port")

        }
      }

      " runs in CONTRACTION mode" in {
        CLIParser.parse(Array("--port","27040","--source","192.168.1.44","--mongoPort","27019","--mode","CONTRACTION")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.CONTRACTION
            config.deltasDir mustEqual defaultDirURI
            success

          case None =>
            failure("Should have run in CONTRACTION mode")

        }
      }

      " uses the specified directory for picking up delta files " in {
        CLIParser.parse(Array("--port", "27040", "--source", "192.168.1.44",
          "--mongoPort", "27019", "--mode", "CONTRACTION", "--deltasDir", "src/test/scala/com/ee/midas/newDeltas")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.CONTRACTION
            config.deltasDir mustEqual userSuppliedURI
            success

          case None =>
            failure("Should have used specified deltas directory")

        }
      }
    }
}
