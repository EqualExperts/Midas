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

import java.net.{Socket, InetAddress}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{After, Specification}
import com.ee.midas.transform.TransformType
import java.io._
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import scala.Some

@RunWith(classOf[JUnitRunner])
class ConfigurationSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val deltasDir = new File("/" + System.getProperty("user.dir"))
    val midasConfigFile = new File(deltasDir.getPath + "midas.config")
    midasConfigFile.createNewFile()
    midasConfigFile.deleteOnExit()
    val appName = "testApp"
    val nonExistentAppName = "nonExistentApp"

    val midasConfigText =
      s"""
      |apps {
      |  $appName
      |  $nonExistentAppName
      |}
     """.stripMargin
    write(midasConfigText, midasConfigFile)

    val ip1 = "127.0.0.1"
    val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)

    val ip2 = "127.0.0.2"
    val (node2Name, node2Ip, changeSet2) = ("node2", InetAddress.getByName(ip2), 2)
    val appConfigDir = new File(deltasDir + File.separator + appName)
    appConfigDir.mkdirs()
    appConfigDir.deleteOnExit()

    val appConfigFile = new File(appConfigDir.getPath + File.separator + s"$appName.midas")
    appConfigFile.deleteOnExit()
    val appConfigText =
      s"""
      |$appName {
      |  mode = expansion
      |  $node1Name {
      |    ip = $ip1
      |    changeSet = $changeSet1
      |  }
      |  $node2Name {
      |    ip = $ip2
      |    changeSet = $changeSet2
      |  }
      |}
     """.stripMargin

    write(appConfigText, appConfigFile)

    val node1 = new Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val node2 = new Node(node2Name, node2Ip, ChangeSet(changeSet2))
    val nodes = Set(node1, node2)
    val application = new Application(appConfigDir.toURI.toURL, appName, TransformType.EXPANSION, nodes)
    val configuration = new Configuration(deltasDir.toURI.toURL, List(appName, nonExistentAppName))
  }

  "Configuration" should {

    "Manage Applications" in {
      "By allowing a Application to be retrieved by IP" in new Setup {
        //When-Then
        configuration.getApplication(node1Ip) mustEqual Some(application)
      }

      "By giving no result when Application with that IP is not present" in new Setup {
        //Given
        val ip = InetAddress.getByName("127.0.0.9")

        //When
        val app = configuration.getApplication(ip)

        //Then
        app mustEqual None
      }

      "Give all applications" in new Setup {
        //When-Then
        configuration.applications mustEqual List(application)
      }
    }

    "Forward authorized connection to corresponding application" in new Setup with After {
      //Given
      val servers = new ServerSetup()
      servers.start
      def after = {
        servers.stop
        configuration.stop
      }

      val clientSocket = new Socket("localhost", servers.midasServerPort)
      val mongoSocket = new Socket("localhost", servers.mongoServerPort)

      //When
      configuration.processNewConnection(clientSocket, mongoSocket)

      //Then
      configuration.getApplication(node1Ip).get.isActive must beTrue
    }

    "Reject unauthorized connection" in new Setup {
      //Given
      val appSocket = mock[Socket]
      val mongoSocket = mock[Socket]
      appSocket.getInetAddress returns InetAddress.getByName("127.0.0.9")

      //When
      configuration.processNewConnection(appSocket, mongoSocket)

      //Then
      there was one(appSocket).close
    }

    "stop all the applications" in new Setup with After {
      //Given
      val servers = new ServerSetup()
      servers.start
      def after = {
        servers.stop
      }

      val clientSocket1 = new Socket("localhost", servers.midasServerPort)
      val mongoSocket1 = new Socket("localhost", servers.mongoServerPort)
      configuration.processNewConnection(clientSocket1, mongoSocket1)

      val clientSocket2 = new Socket("localhost", servers.midasServerPort)
      val mongoSocket2 = new Socket("localhost", servers.mongoServerPort)
      configuration.processNewConnection(clientSocket2, mongoSocket2)

      //When
      configuration.stop

      //Then
      configuration.getApplication(node1Ip).get.isActive must beFalse
    }
  }
}
