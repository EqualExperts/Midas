package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.BeforeAfter
import com.ee.midas.transform.TransformType
import java.net.{URL, URI}
import java.io.File

@RunWith(classOf[JUnitRunner])
class CLIParserSpecs extends Specification {

  trait SetupTeardown extends BeforeAfter {
    val loader = this.getClass.getClassLoader
    val expansionFolder =  TransformType.EXPANSION.toString.toLowerCase
    val contractionFolder =  TransformType.CONTRACTION.toString.toLowerCase
    val baseDeltasDirURI : URI =  loader.getResource("deltas").toURI
    val expansionDeltasDirURL: URL = new File(baseDeltasDirURI.getPath + "/" + expansionFolder).toURI.toURL
    val contractionDeltasDirURL: URL = new File(baseDeltasDirURI.getPath + "/" + contractionFolder).toURI.toURL
    val newBaseDeltasDir = "src/test/scala/com/ee/midas/newDeltas"
    val newExpansionDeltaURI = newBaseDeltasDir + "/" + expansionFolder
    val newContractionDeltaURI = newBaseDeltasDir + "/" + contractionFolder
    val newBaseDeltasdirFile = new File(newBaseDeltasDir)
    val newExpansionDeltasdirFile = new File(newExpansionDeltaURI)
    val newContractionDeltasdirFile = new File(newContractionDeltaURI)
    val userSuppliedURI = new File(newBaseDeltasDir).toURI

    def before: Any = {
      newBaseDeltasdirFile.mkdir()
      newExpansionDeltasdirFile.mkdir()
      newContractionDeltasdirFile.mkdir()
    }

    def after: Any = {
      newExpansionDeltasdirFile.delete
      newContractionDeltasdirFile.delete
      newBaseDeltasdirFile.delete
    }
  }

  sequential

    "Midas" should {
       "run with default values" in new SetupTeardown {
         CLIParser.parse(Array()) match {
           case Some(config) =>
             config.midasHost mustEqual "localhost"
             config.midasPort mustEqual 27020
             config.mongoHost mustEqual "localhost"
             config.mongoPort mustEqual 27017
             config.mode mustEqual TransformType.EXPANSION
             config.baseDeltasDir mustEqual baseDeltasDirURI
             config.deltasDirURL mustEqual expansionDeltasDirURL
             success

           case None =>
              failure("Should have run with default Values")
         }
       }

      "run on a given HOST and connect to default source and mongoPort" in new SetupTeardown {
        CLIParser.parse(Array("--host","www.midasservice.in")) match {
          case Some(config) =>
            config.midasHost mustEqual "www.midasservice.in"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given PORT while using defaults for source and mongoPort")

        }
      }

      "run on a given PORT and connect to default source and mongoPort" in new SetupTeardown {
        CLIParser.parse(Array("--port","27040")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given PORT while using defaults for source and mongoPort")

        }
      }

      " run on default port and connect to given MONGOHOST on default mongoPort" in new SetupTeardown {
        CLIParser.parse(Array("--source","192.168.1.44")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given MONGOHOST while using defaults for port and mongoPort")

        }
      }

      " run on default port and connect to default mongoHost on given MONGOPORT" in new SetupTeardown {
        CLIParser.parse(Array("--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given MONGOPORT while using defaults for port and mongoHost")

        }
      }

      " run on given PORT and connect to given MONGOHOST on default mongoPort" in new SetupTeardown {
        CLIParser.parse(Array("--port","27040","--source","192.168.1.44")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27017
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given PORT and MONGOHOST while using defaults for mongoPort")

        }
      }

      " run on given PORT and connect to default mongoHost on given MONGOPORT" in new SetupTeardown {
        CLIParser.parse(Array("--port","27040","--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given PORT and MONGOPORT while using defaults for mongoHost")

        }
      }

      " run on default port and connect to given MONGOHOST on MONGOPORT" in new SetupTeardown {
        CLIParser.parse(Array("--source","192.168.1.44","--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.EXPANSION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual expansionDeltasDirURL
            success

          case None =>
            failure("Should have run with given MONGOHOST and MONGOPORT while using defaults for port")

        }
      }

      " run in CONTRACTION mode" in new SetupTeardown {
        CLIParser.parse(Array("--host","www.midasservice.in","--port","27040","--source","192.168.1.44","--mongoPort","27019","--mode","CONTRACTION")) match {
          case Some(config) =>
            config.midasHost mustEqual "www.midasservice.in"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.CONTRACTION
            config.baseDeltasDir mustEqual baseDeltasDirURI
            config.deltasDirURL mustEqual contractionDeltasDirURL
            success

          case None =>
            failure("Should have run in CONTRACTION mode")

        }
      }

      " use the specified directory for picking up delta files " in new SetupTeardown {
        CLIParser.parse(Array("--port", "27040", "--source", "192.168.1.44",
          "--mongoPort", "27019", "--mode", "CONTRACTION", "--deltasDir", newBaseDeltasDir)) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.mode mustEqual TransformType.CONTRACTION
            config.baseDeltasDir mustEqual newBaseDeltasdirFile.toURI
            config.deltasDirURL mustEqual newContractionDeltasdirFile.toURI.toURL
            success

          case None =>
            failure("Should have used specified deltas directory")

        }
      }

      "fails when given a deltasDir that doesn't exist" in {
        CLIParser.parse(Array("--deltasDir", "someDir/someFile")) match {
          case None =>
            success
          case Some(config) =>
            failure("Should have failed for a directory that doesn't exist")
        }
      }

      "fails when an invalid option is given" in {
        CLIParser.parse(Array("--invalidOption", "invalidValue")) match {
          case None =>
            success
          case Some(config) =>
            failure("Should have failed for an invalid option")
        }
      }
    }
}
