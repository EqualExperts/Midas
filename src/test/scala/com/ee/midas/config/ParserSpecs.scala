package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import java.net.{InetAddress}
import com.ee.midas.transform.TransformType

@RunWith(classOf[JUnitRunner])
class ParserSpecs extends Specification {
  sequential

  trait ConfigParser extends Parser with Scope {
    def Result[T](result: ParseResult[T]): T = result match {
      case Success(value, _) =>  value
      case NoSuccess(message, _) => throw new IllegalArgumentException(s"Parsing Failed Message: $message")
    }
  }

  "Parser" should {
    "Parse change set" in {
      "valid changeset" in new ConfigParser {
        //Given
        val input = "changeSet = 1"

        //When
        val chgSet = Result(parseAll(changeSet, input))

        //Then
        chgSet mustEqual ChangeSet(1)
      }
    }

    "Not Parse change set" in {
      "double format" in new ConfigParser {
        //Given
        val input = "changeSet = 1.0"

        //When-Then
        Result(parseAll(changeSet, input)) must throwA[IllegalArgumentException]
      }

      "string format" in new ConfigParser {
        //Given
        val input = """changeSet = "1.0"""

        //When-Then
        Result(parseAll(changeSet, input)) must throwA[IllegalArgumentException]
      }

      "empty format" in new ConfigParser {
        //Given
        val input = "changeSet = "

        //When-Then
        Result(parseAll(changeSet, input)) must throwA[IllegalArgumentException]
      }

    }

    "Parse IP" in {
      "valid ipv4 host" in new ConfigParser {
        //Given
        val input = "ip = 127.0.0.1"

        //When
        val ipv4Address = Result(parseAll(ip, input))

        //Then
        ipv4Address mustEqual InetAddress.getByName("127.0.0.1")
      }

      "valid ipv4 host" in new ConfigParser {
        //Given
        val input = "ip = 1.1.1.1"

        //When
        val ipv4Address = Result(parseAll(ip, input))

        //Then
        ipv4Address mustEqual InetAddress.getByName("1.1.1.1")
      }
    }

    "Not Parse IP" in {
      "ipv4 host name" in new ConfigParser {
        "valid ipv4 host name" in new ConfigParser {
          //Given
          val input = "ip = java.sun.com"

          //When-Then
          Result(parseAll(ip, input)) must throwA[IllegalArgumentException]
        }
      }

      "compressed ipv6" in new ConfigParser {
        //Given
        val input = "ip = 2001:db8::ff00:42:8329"

        //When-Then
        Result(parseAll(ip, input)) must throwA[IllegalArgumentException]
      }

      "multi-cast compressed ipv6" in new ConfigParser {
        //Given
        val input = "ip = FF02::2"

        //When-Then
        Result(parseAll(ip, input)) must throwA[IllegalArgumentException]
      }

    }

    "Parse mode" in {
      "valid contraction mode" in new ConfigParser {
        //Given
        val input = "mode = contraction"

        //When
        val transformType = Result(parseAll(mode, input))

        //Then
        transformType mustEqual TransformType.CONTRACTION
      }

      "valid expansion mode" in new ConfigParser {
        //Given
        val input = "mode = expansion"

        //When
        val transformType = Result(parseAll(mode, input))

        //Then
        transformType mustEqual TransformType.EXPANSION
      }
    }

    "Not Parse mode" in {
      "invalid mode" in new ConfigParser {
        //Given
        val input = "mode = invalid"

        //When-Then
        Result(parseAll(mode, input)) must throwA[IllegalArgumentException]
      }

      "empty mode" in new ConfigParser {
        //Given
        val input = "mode = "

        //When-Then
        Result(parseAll(mode, input)) must throwA[IllegalArgumentException]
      }
    }

    "Parse node" in {
      "valid node configuration" in new ConfigParser {
        //Given
        val nodeName = "node1"
        val localhost = "127.0.0.1"
        val cs = 1L
        val input =
          s"""
            |$nodeName {
            |  ip = $localhost,
            |  changeSet = $cs
            |}
          """.stripMargin

        //When
        val aNode = Result(parseAll(node, input))

        //Then
        aNode mustEqual Node(nodeName, InetAddress.getByName(localhost), ChangeSet(cs))
      }
    }

    "Parse application" in {

      "valid application configuration with single node" in new ConfigParser {
        //Given
        val appName = "testApp"
        val nodeName = "node1"
        val localhost = "127.0.0.1"
        val cs = 1L
        val input =
          s"""
            |$appName {
            |  mode = contraction,
            |  $nodeName {
            |    ip = $localhost,
            |    changeSet = $cs
            |  }
            |}
          """.stripMargin

        //When
        val appNode = Result(parseAll(app, input))

        //Then
        appNode mustEqual Application(appName, TransformType.CONTRACTION, List(Node(nodeName, InetAddress.getByName(localhost), ChangeSet(cs))))
      }

     "valid application configuration with multiple nodes" in new ConfigParser {
       //Given
       val appName = "testApp"
       val node1 = "node1"
       val node2 = "node2"
       val ip1 = "127.0.0.1"
       val ip2 = "127.0.0.1"
       val cs = 1L
       val input =
         s"""
            |$appName {
            |  mode = expansion,
            |  $node1 {
            |    ip = $ip1,
            |    changeSet = $cs
            |  }
            |  $node2 {
            |    ip = $ip2,
            |    changeSet = $cs
            |  }
            |}
          """.stripMargin

       //When
       val appNode = Result(parseAll(app, input))

       //Then
       appNode mustEqual Application(appName,
         TransformType.EXPANSION,
         List(
           Node(node1, InetAddress.getByName(ip1), ChangeSet(cs)),
           Node(node2, InetAddress.getByName(ip2), ChangeSet(cs))
         ))
     }
   }

    "Not Parse application" in {
      "configuration without node" in new ConfigParser {
        //Given
        val appName = "testApp"
        val input =
          s"""
            |$appName {
            |  mode = contraction
            |}
          """.stripMargin

        //When-Then
        Result(parseAll(app, input)) must throwA[IllegalArgumentException]
      }
    }

    "Parse applications" in {

      "in empty configuration" in new ConfigParser {
        //Given
        val input =
          s"""
            |apps {
            |}
          """.stripMargin

        //When
        val appsNode = Result(parseAll(apps, input))

        //Then
        appsNode mustEqual Nil
      }

      "in configuration with single application" in new ConfigParser {
        //Given
        val appName = "testApp"
        val node1 = "node1"
        val node2 = "node2"
        val ip1 = "127.0.0.1"
        val ip2 = "127.0.0.1"
        val cs = 1L
        val input =
          s"""
            |apps {
            |  $appName {
            |    mode = expansion,
            |    $node1 {
            |      ip = $ip1,
            |      changeSet = $cs
            |    }
            |    $node2 {
            |      ip = $ip2,
            |      changeSet = $cs
            |    }
            |  }
            |}
          """.stripMargin

        //When
        val appNode = Result(parseAll(apps, input))

        //Then
        appNode mustEqual List(
            Application(appName,
            TransformType.EXPANSION,
            List(
              Node(node1, InetAddress.getByName(ip1), ChangeSet(cs)),
              Node(node2, InetAddress.getByName(ip2), ChangeSet(cs))
            )))
      }

      "in configuration with multiple applications" in new ConfigParser {
        //Given
        val app1 = "testApp1"
        val app1Node1 = "node1"
        val app2Node2 = "node2"
        val ip1 = "127.0.0.1"
        val ip2 = "127.0.0.1"
        val cs = 1L
        val app2 = "testApp2"
        val app2Node = "node3"
        val ip3 = "192.6.1.10"
        val cs2 = 3L
        val input =
          s"""
            |apps {
            |  $app1 {
            |    mode = expansion,
            |    $app1Node1 {
            |      ip = $ip1,
            |      changeSet = $cs
            |    }
            |    $app2Node2 {
            |      ip = $ip2,
            |      changeSet = $cs
            |    }
            |  }
            |
            |  $app2 {
            |    mode = contraction,
            |    $app2Node {
            |      ip = $ip3,
            |      changeSet = $cs2
            |    }
            |  }
            |}
          """.stripMargin

        //When
        val appNode = Result(parseAll(apps, input))

        //Then
        appNode mustEqual List(
          Application(app1,
            TransformType.EXPANSION,
            List(
              Node(app1Node1, InetAddress.getByName(ip1), ChangeSet(cs)),
              Node(app2Node2, InetAddress.getByName(ip2), ChangeSet(cs))
            )),
          Application(app2,
            TransformType.CONTRACTION,
            List(
              Node(app2Node, InetAddress.getByName(ip3), ChangeSet(cs2))
            ))
        )
      }
    }
  }
}
