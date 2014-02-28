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