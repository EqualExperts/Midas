package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType
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
        config mustEqual Configuration(ignore, Nil)
      }

//      "in configuration with single application" in new ConfigParser {
//        //Given
//        val appName = "testApp"
//        val node1 = "node1"
//        val node2 = "node2"
//        val ip1 = "127.0.0.1"
//        val ip2 = "127.0.0.1"
//        val cs = 1L
//        val input =
//          s"""
//            |apps {
//            |  $appName {
//            |    mode = expansion
//            |    $node1 {
//            |      ip = $ip1
//            |      changeSet = $cs
//            |    }
//            |    $node2 {
//            |      ip = $ip2
//            |      changeSet = $cs
//            |    }
//            |  }
//            |}
//          """.stripMargin
//
//        //When
//        val config = Result(parseAll(configuration, input))
//
//        //Then
//        val apps = List(
//          Application(appName, TransformType.EXPANSION,
//            List(
//              Node(node1, InetAddress.getByName(ip1), ChangeSet(cs)),
//              Node(node2, InetAddress.getByName(ip2), ChangeSet(cs))
//            )))
//
//        config mustEqual Configuration(apps)
//      }

//      "in configuration with multiple applications" in new ConfigParser {
//        //Given
//        val app1 = "testApp1"
//        val app1Node1 = "node1"
//        val app2Node2 = "node2"
//        val ip1 = "127.0.0.1"
//        val ip2 = "127.0.0.1"
//        val cs = 1L
//        val app2 = "testApp2"
//        val app2Node = "node3"
//        val ip3 = "192.6.1.10"
//        val cs2 = 3L
//        val input =
//          s"""
//            |apps {
//            |  $app1 {
//            |    mode = expansion
//            |    $app1Node1 {
//            |      ip = $ip1
//            |      changeSet = $cs
//            |    }
//            |    $app2Node2 {
//            |      ip = $ip2
//            |      changeSet = $cs
//            |    }
//            |  }
//            |
//            |  $app2 {
//            |    mode = contraction
//            |    $app2Node {
//            |      ip = $ip3
//            |      changeSet = $cs2
//            |    }
//            |  }
//            |}
//          """.stripMargin
//
//        //When
//        val config = Result(parseAll(configuration, input))
//
//        //Then
//        val application1 = Application(app1, TransformType.EXPANSION,
//          List(
//            Node(app1Node1, InetAddress.getByName(ip1), ChangeSet(cs)),
//            Node(app2Node2, InetAddress.getByName(ip2), ChangeSet(cs))
//          ))
//        val application2 = Application(app2, TransformType.CONTRACTION,
//          List(
//            Node(app2Node, InetAddress.getByName(ip3), ChangeSet(cs2))
//          ))
//
//        config mustEqual Configuration(application1 :: application2 :: Nil)
//      }
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

      val path: String = "/" + System.getProperty("user.dir")
      val file = new File(path + "/midas.config")
      file.createNewFile()
      file.deleteOnExit()
      val writer = new PrintWriter(file, "utf-8")
      writer.write(configText)
      writer.flush()
      writer.close()

      //When
      val config = parse(file.toURI.toURL)

      //Then
      val nodeA = Node("nodeA", InetAddress.getByName("127.0.0.1"), ChangeSet(2))
      config mustEqual Application("appName", TransformType.EXPANSION, List(nodeA))
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
      config mustEqual Configuration(ignore, Nil)
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
      config mustEqual Configuration(ignore, Nil)
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