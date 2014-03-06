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
import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType._
import java.io.{PrintWriter, File}
import org.specs2.specification.Scope


@RunWith(classOf[JUnitRunner])
class ApplicationParsersSpecs extends Specification {
  sequential

  trait AppConfigParsers extends ApplicationParsers with Scope

  "ChangeSet Parser" should {
    "Parse change set" in new AppConfigParsers {
      "valid change set format" in {
        //Given
        val input = "changeSet = 1"

        //When
        val chgSet = parseAll(changeSet, input).get

        //Then
        chgSet mustEqual ChangeSet(1)
      }
    }

    "Not Parse change set" in new AppConfigParsers {
      "double format" in {
        //Given
        val input = "changeSet = 1.0"

        //When-Then
        parseAll(changeSet, input) mustEqual NoSuccess
      }

      "string format" in {
        //Given
        val input = """changeSet = "1.0"""

        //When-Then
        parseAll(changeSet, input) mustEqual NoSuccess
      }

      "empty format" in {
        //Given
        val input = "changeSet = "

        //When-Then
        parseAll(changeSet, input) mustEqual NoSuccess
      }
    }
  }

  "IP Parser" should {
    "Parse IP" in new AppConfigParsers {
      "valid ipv4 host" in {
        //Given
        val input = "ip = 127.0.0.1"

        //When
        val ipv4Address = parseAll(ip, input).get

        //Then
        ipv4Address mustEqual InetAddress.getByName("127.0.0.1")
      }

      "valid ipv4 host" in {
        //Given
        val input = "ip = 1.1.1.1"

        //When
        val ipv4Address = parseAll(ip, input).get

        //Then
        ipv4Address mustEqual InetAddress.getByName("1.1.1.1")
      }
    }

    "Not Parse IP" in new AppConfigParsers {
      "ipv4 host name" in {
        "valid ipv4 host name" in {
          //Given
          val input = "ip = java.sun.com"

          //When-Then
          parseAll(ip, input) mustEqual NoSuccess
        }
      }

      "compressed ipv6" in {
        //Given
        val input = "ip = 2001:db8::ff00:42:8329"

        //When-Then
        parseAll(ip, input) mustEqual NoSuccess
      }

      "multi-cast compressed ipv6" in {
        //Given
        val input = "ip = FF02::2"

        //When-Then
        parseAll(ip, input) mustEqual NoSuccess
      }
    }
  }
  
  "Mode Parser" should {
    "Parse mode" in new AppConfigParsers {
      "valid contraction mode" in {
        //Given
        val input = "mode = contraction"

        //When
        val transformType = parseAll(mode, input).get

        //Then
        transformType mustEqual CONTRACTION
      }

      "valid expansion mode" in {
        //Given
        val input = "mode = expansion"

        //When
        val transformType = parseAll(mode, input).get

        //Then
        transformType mustEqual EXPANSION
      }
    }

    "Not Parse mode" in new AppConfigParsers {
      "invalid mode" in {
        //Given
        val input = "mode = invalid"

        //When-Then
        parseAll(mode, input) mustEqual NoSuccess
      }

      "empty mode" in {
        //Given
        val input = "mode = "

        //When-Then
        parseAll(mode, input) mustEqual NoSuccess
      }
    }
  }
  
  "Node Parser" should {
    "Parse node" in new AppConfigParsers {
      "valid node configuration" in {
        //Given
        val nodeName = "node1"
        val localhost = "127.0.0.1"
        val cs = 1L
        val input =
          s"""
            |$nodeName {
            |  ip = $localhost
            |  changeSet = $cs
            |}
          """.stripMargin

        //When
        val aNode = parseAll(node, input).get

        //Then
        aNode mustEqual (new Node(nodeName, InetAddress.getByName(localhost), ChangeSet(cs)))
      }
      
      "yet another valid node configuration" in {
        //Given
        val nodeName = "node1"
        val localhost = "127.0.0.1"
        val cs = 1L
        val input =
          s"""
            |$nodeName 
            |{
            |  ip = $localhost
            |  changeSet = $cs
            |}
          """.stripMargin

        //When
        val aNode = parseAll(node, input).get

        //Then
        aNode mustEqual new Node(nodeName, InetAddress.getByName(localhost), ChangeSet(cs))
      }
    }
  }

  "Application Parser" should {
    "Parse application" in new AppConfigParsers {
      "valid application configuration with single node" in {
        //Given
        val appName = "testApp"
        val nodeName = "node1"
        val localhost = "127.0.0.1"
        val cs = 1L
        val input =
          s"""
            |$appName {
            |  mode = contraction
            |  $nodeName {
            |    ip = $localhost
            |    changeSet = $cs
            |  }
            |}
          """.stripMargin
        val ignoreConfig: URL = null

        //When
        val application = parseAll(app(ignoreConfig), input).get

        //Then
        val expected = new Application(ignoreConfig, appName, CONTRACTION,
          Set(new Node(nodeName, InetAddress.getByName(localhost), ChangeSet(cs))))

        application mustEqual expected
      }

     "valid application configuration with multiple nodes" in {
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
            |  mode = expansion
            |  $node1 {
            |    ip = $ip1
            |    changeSet = $cs
            |  }
            |  $node2 {
            |    ip = $ip2
            |    changeSet = $cs
            |  }
            |}
          """.stripMargin
       val ignoreConfig: URL = null


       //When
       val application = parseAll(app(ignoreConfig), input).get

       //Then
       val expected = new Application(ignoreConfig, appName, EXPANSION,
         Set(
           new Node(node1, InetAddress.getByName(ip1), ChangeSet(cs)),
           new Node(node2, InetAddress.getByName(ip2), ChangeSet(cs))
         ))

       application mustEqual expected
     }
   }

    "Fail to Parse application" in new AppConfigParsers {
      "app config without node" in {
        //Given
        val appName = "testApp"
        val input =
          s"""
            |$appName {
            |  mode = contraction
            |}
          """.stripMargin
        val ignoreConfig: URL = null

        //When-Then
        parseAll(app(ignoreConfig), input) mustEqual NoSuccess
      }
    }

    "Parses application" in new AppConfigParsers {
      val appName = "appNameBeta2"
      val nodeIp = "127.0.0.1"
      val nodeChangeSet = 2
      val configText =
        s"""
          |$appName {
          |  mode = expansion
          |  nodeA {
          |    ip = $nodeIp
          |    changeSet = $changeSet
          |  }
          |}
         """.stripMargin

      val deltasDir = new File("/" + System.getProperty("user.dir")).toURI.toURL
      val appDir = "app"
      val appConfigDir = new File(deltasDir.getPath + appDir)
      appConfigDir.mkdirs()
      appConfigDir.deleteOnExit()

      val appConfigFilename = s"${appDir}.midas"
      val file = new File(deltasDir.getPath + appDir + File.separator + appConfigFilename)
      file.createNewFile()
      file.deleteOnExit()
      val writer = new PrintWriter(file, "utf-8")
      writer.write(configText)
      writer.flush()
      writer.close()


      "defined by Text" in {
        //Given
        val ignoreConfig: URL = null

        //When
        val application = parse(configText, ignoreConfig)

        //Then
        val nodeA = new Node("nodeA", InetAddress.getByName(nodeIp), ChangeSet(nodeChangeSet))
        application mustEqual (new Application(ignoreConfig, appName, EXPANSION, Set(nodeA)))
      }

      "defined by URL" in {
        val appConfig = appConfigDir.toURI.toURL
        //When
        val application = parse(appConfig).get

        //Then
        val nodeA = new Node("nodeA", InetAddress.getByName(nodeIp), ChangeSet(nodeChangeSet))
        application mustEqual (new Application(appConfig, appName, EXPANSION, Set(nodeA)))
      }
    }
  }

  "Parsers Eat Java-Style Comments" in new AppConfigParsers {
    val appName = "appName"
    val nodeName = "nodeA"
    val nodeIp = "127.0.0.1"
    val cs = 1
    val nodeA = new Node(nodeName, InetAddress.getByName(nodeIp), ChangeSet(cs))

    "configuration containing single line comments" in {
      //Given

      val input =
        s"""
            |// Single-Line comment
            |$appName {
            |  mode = expansion
            |  $nodeName {   // new Node
            |    ip = $nodeIp
            |    changeSet = $cs
            |  }
            |} // End of application
          """.stripMargin
      val ignoreConfig: URL = null

      //When
      val application = parseAll(app(ignoreConfig), input).get

      //Then
      application mustEqual (new Application(ignoreConfig, appName, EXPANSION, Set(nodeA)))
    }

    "configuration containing multi line comments" in {
      //Given
      val input =
        s"""
            |/**
            | * Multi-Line comment
            | */
            |$appName {
            |  mode = expansion
            |  $nodeName {
            |    ip = $nodeIp
            |    /**
            |     * This changeset was changed by Dhaval
            |     */
            |    changeSet = $cs
            |  }
            |}
          """.stripMargin
      val ignoreConfig: URL = null

      //When
      val application = parseAll(app(ignoreConfig), input).get

      //Then
      application mustEqual (new Application(ignoreConfig, appName, EXPANSION, Set(nodeA)))
    }
  }

  "Parsers Fails to Eat Java-Style Comments" in new AppConfigParsers {
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
      val ignoreConfig: URL = null

      //When-Then
      parseAll(app(ignoreConfig), input) mustEqual NoSuccess
    }
  }
}
