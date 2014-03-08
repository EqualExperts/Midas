/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

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
    val defaultBaseDeltasDirURI : URI =  loader.getResource("deltas").toURI
    val defaultMidasConfigURL : URL = new URL(defaultBaseDeltasDirURI.toString + "/midas.config")
    val defaultExpansionDeltasDirURL: URL = new File(defaultBaseDeltasDirURI.getPath + "/" + expansionFolder).toURI.toURL
    val defaultContractionDeltasDirURL: URL = new File(defaultBaseDeltasDirURI.getPath + "/" + contractionFolder).toURI.toURL

    val newBaseDeltasDir = "test-data/cliParserSpecs/deltas"
    val newExpansionDeltaURI = newBaseDeltasDir + "/" + expansionFolder
    val newContractionDeltaURI = newBaseDeltasDir + "/" + contractionFolder
    val newBaseDeltasdirFile = new File(newBaseDeltasDir)
    val newExpansionDeltasdirFile = new File(newExpansionDeltaURI)
    val newContractionDeltasdirFile = new File(newContractionDeltaURI)
    val userSuppliedURI = new File(newBaseDeltasDir).toURI

    def before: Any = {
      newBaseDeltasdirFile.mkdirs()
      newExpansionDeltasdirFile.mkdirs()
      newContractionDeltasdirFile.mkdirs()
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
             config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
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
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
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
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
            success

          case None =>
            failure("Should have run with given PORT while using defaults for source and mongoPort")

        }
      }

      "run on default port and connect to given MONGOHOST on default mongoPort" in new SetupTeardown {
        CLIParser.parse(Array("--source","192.168.1.44")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27020
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27017
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
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
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
            success

          case None =>
            failure("Should have run with given MONGOPORT while using defaults for port and mongoHost")

        }
      }

      "run on given PORT and connect to given MONGOHOST on default mongoPort" in new SetupTeardown {
        CLIParser.parse(Array("--port","27040","--source","192.168.1.44")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27017
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
            success

          case None =>
            failure("Should have run with given PORT and MONGOHOST while using defaults for mongoPort")

        }
      }

      "run on given PORT and connect to default mongoHost on given MONGOPORT" in new SetupTeardown {
        CLIParser.parse(Array("--port","27040","--mongoPort","27019")) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "localhost"
            config.mongoPort mustEqual 27019
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
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
            config.baseDeltasDir mustEqual defaultBaseDeltasDirURI
            success

          case None =>
            failure("Should have run with given MONGOHOST and MONGOPORT while using defaults for port")

        }
      }

      "use the specified directory for picking up delta files " in new SetupTeardown {
        CLIParser.parse(Array("--port", "27040", "--source", "192.168.1.44",
          "--mongoPort", "27019", "--deltasDir", newBaseDeltasDir)) match {
          case Some(config) =>
            config.midasHost mustEqual "localhost"
            config.midasPort mustEqual 27040
            config.mongoHost mustEqual "192.168.1.44"
            config.mongoPort mustEqual 27019
            config.baseDeltasDir mustEqual newBaseDeltasdirFile.toURI
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
