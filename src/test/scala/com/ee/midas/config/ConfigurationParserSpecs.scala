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

package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import java.net.{URL}
import java.io.{PrintWriter, File}
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ConfigurationParserSpecs extends Specification {
  sequential

  trait ConfigParser extends ConfigurationParser with Scope

  "Configuration Parser" should {
    "Parse configuration" in new ConfigParser {

      "as empty" in {
        //Given
        val input =
          s"""
            |apps {
            |}
          """.stripMargin

        val ignore: URL = null

        //When
        val config = parseAll(configuration(ignore), input).get

        //Then
        config mustEqual (new Configuration(ignore, Nil))
      }

      "in configuration with single application" in new ConfigParser {
        //Given
        val appName = "testApp"
        val input =
          s"""
            |apps {
            |  $appName
            |}
          """.stripMargin
        val ignore: URL = null

        //When
        val config = parseAll(configuration(ignore), input)

        //Then
        val apps = List(appName)

        config mustEqual (new Configuration(ignore, apps))
      }

      "in configuration with multiple applications" in new ConfigParser {
        //Given
        val app1 = "testApp1"
        val app2 = "testApp2"
        val input =
          s"""
            |apps {
            |  $app1
            |  $app2
            |}
          """.stripMargin

        val ignore: URL = null

        //When
        val config = parseAll(configuration(ignore), input)

        //Then
        config mustEqual (new Configuration(ignore, List(app1, app2)))
      }
    }
  }

  "Parses configuration" in new ConfigParser {
    "defined by URL" in {
      //Given
      val configText =
        """
          |apps {
          |  app1
          |}
        """.stripMargin

      val deltasDir = new File("/" + System.getProperty("user.dir")).toURI.toURL
      val midasConfigFile = new File(deltasDir.getPath + Configuration.filename)
      midasConfigFile.createNewFile()
      midasConfigFile.deleteOnExit()
      val writer = new PrintWriter(midasConfigFile, "utf-8")
      writer.write(configText)
      writer.flush()
      writer.close()

      //When
      val config = parse(deltasDir, Configuration.filename)

      //Then
      config mustEqual (new Configuration(deltasDir, List("app1")))
    }
  }

  "Eats Java-Style Comments" in new ConfigParser {
    "configuration containing single line comments" in {
      //Given
      val input =
        s"""
            |// Single-Line comment
            |apps {
            |} // End of application
          """.stripMargin
      val ignore: URL = null

      //When
      val config = parseAll(configuration(ignore), input).get

      //Then
      config mustEqual (new Configuration(ignore, Nil))
    }

    "configuration containing multi line comments" in {
      //Given
      val input =
        s"""
            |/**
            | * Multi-Line comment
            | */
            |apps {
            |
            |}
          """.stripMargin

      val ignore: URL = null

      //When
      val config = parseAll(configuration(ignore), input).get

      //Then
      config mustEqual (new Configuration(ignore, Nil))
    }
  }

  "Fails to Eat Java-Style Comments" in new ConfigParser {
    "configuration containing nested comments" in {
      //Given
      val input =
        s"""
            |/**
            | * Multi-Line comment
            | * /*
            | *  * Nested comment
            | *  */
            | */
            |apps {
            |}
          """.stripMargin
      val ignore: URL = null

      //When-Then
      parseAll(configuration(ignore), input) mustEqual NoSuccess
    }
  }
}