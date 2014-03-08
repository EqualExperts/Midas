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

package com.ee.midas.model

import org.junit.runner.RunWith
import java.net.{Socket, URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.specs2.mock.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.{Ignore, AfterClass, BeforeClass, Test}
import com.ee.midas.dsl.Translator
import java.io.{StringReader, File}
import scala.util.Try
import com.ee.midas.utils.SynchronizedHolder

/**
 * IMPORTANT NOTE:
 *
 * Specs are written in JUnit style because
 * 1) Conventional style causes the test cases to run multiple times, as described in the link below.
 * https://groups.google.com/forum/#!topic/play-framework/4Fz5TsOKPio
 * 2) This causes numerous problems while testing systems with multi-threaded environment.
 *
 * Also, @RunWith uses MockitoJUnitRunner because the conventional JUnitRunner provided by Specs2
 * is not compatible with JUnit style test cases written here.
 */

@RunWith(classOf[MockitoJUnitRunner])
class ApplicationSpecs extends JUnitMustMatchers with Mockito {

  val appName = "app1"
  val nonExistentAppName = "nonExistentApp"

  val ip1 = "127.0.0.1"
  val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)

  val ip2 = "127.0.0.0"
  val (node2Name, node2Ip, changeSet2) = ("node2", InetAddress.getByName(ip2), 2)

  val node1 = new Node(node1Name, node1Ip, ChangeSet(changeSet1))
  val node2 = new Node(node2Name, node2Ip, ChangeSet(changeSet2))
  val nodes = Set(node1, node2)
  val deltasDir = System.getProperty("user.dir")
  val appDir = new File(s"$deltasDir/$appName")
  appDir.mkdir()
  appDir.deleteOnExit()
  val configDir: URL = appDir.toURI.toURL
  val application = new Application(configDir, appName, TransformType.EXPANSION, nodes)

  @Test
  def allowRetrievalOfNodeByIp() {
    //When-Then
    application.getNode(node2Ip) mustEqual Some(node2)
  }

  @Test
  def giveNoResultIfNodeWithThatIpIsNotPresent() {
    //Given
    val ip = InetAddress.getByName("127.0.0.9")

    //When-Then
    application.getNode(ip) mustEqual None
  }

  @Test
  def affirmNodePresenceByIP() {
    //When-Then
    application.getNode(node2Ip) mustEqual Some(node2)
  }

  @Test
  def denyNodePresenceByIP() {
    //Given
    val ip = InetAddress.getByName("127.0.0.6")

    //When-Then
    application.getNode(ip) mustEqual None
  }

  @Test
  def stopNodes() {
    val application = new Application(configDir, appName, TransformType.EXPANSION, Set(node1, node2))

    //When
    application.stop

    //Then
    application.isActive mustEqual false
  }

  @Test
  def acceptsConnectionForNodeAvailableInConfiguration() {
      //Given
      val host = "127.0.0.1"
      val clientSocket = new Socket(host, ApplicationSpecs.servers.midasServerPort)
      val mongoSocket = new Socket(host, ApplicationSpecs.servers.mongoServerPort)
      val application = new Application(configDir, appName, TransformType.EXPANSION, nodes)

      //When
      application.acceptAuthorized(clientSocket, mongoSocket)

      //Then
      node1.isActive mustEqual true
      node2.isActive mustEqual false
    }

  @Test
  def ignoresConnectionFromNodeNotAvailableInConfiguration() {
    //Given
    val client = mock[Socket]
    client.getInetAddress returns InetAddress.getByName("127.0.0.18")
    val mongo = mock[Socket]
    val application = new Application(configDir, appName, TransformType.EXPANSION, nodes)

    //When
    application.acceptAuthorized(client, mongo)

    //Then
    node1.isActive mustEqual false
    node2.isActive mustEqual false
  }

  @Test
  def parseDeltasWhenApplicationIsCreated() {
    //Given
    var processDeltaCalled = 0
    val configDir: URL = new File("test-data/applicationSpecs/deltas/app1").toURI.toURL

    //When
    new Application(configDir, appName, TransformType.EXPANSION, nodes) {
     override def processDeltas(translator: Translator[Transformer], transformType: TransformType, configDir: URL): Try[Transformer] = Try {
        processDeltaCalled = 1
        (Transformer.empty)
      }
    }

    //Then
    processDeltaCalled mustEqual 1
  }


  @Test
  def holderHoldsTransformerWhenApplicationIsCreated() {
    //Given
    val configDir: URL = new File("test-data/applicationSpecs/deltas/app1").toURI.toURL
    val transformer = mock[Transformer]
    val holder = mock[SynchronizedHolder[Transformer]]


    //When
    new Application(configDir, appName, TransformType.EXPANSION, nodes, holder) {
      override def processDeltas(translator: Translator[Transformer], transformType: TransformType, configDir: URL): Try[Transformer] = Try {
        transformer
      }
    }

    //Then
    there was one(holder).apply(transformer)
  }

  @Test
  def updatesApplicationName: Unit = {
    //Given
    val newAppName = "newAppName"
    val newApplication = new Application(configDir, newAppName, TransformType.EXPANSION, nodes)

    //When
    application.update(newApplication)

    //Then
    application.name mustEqual newAppName
  }

  @Test
  def updatesTransformerFromNewApplication: Unit = {
    //Given
    val holder = SynchronizedHolder(Transformer.empty)
    val transformer = new Transformer {
      var responseExpansions: Map[String, VersionedSnippets] = Map()
      var responseContractions: Map[String, VersionedSnippets] = Map()
      var transformType: TransformType = TransformType.CONTRACTION
      var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map()
      var requestContractions: Map[ChangeSetCollectionKey, Double] = Map()
    }

    //And
    val oldApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes, holder)

    val newApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes) {
      override def processDeltas(translator: Translator[Transformer], transformType: TransformType, configDir: URL): Try[Transformer] = Try {
        transformer
      }
    }

    //When
    oldApplication.update(newApplication)

    //Then
    holder.get mustEqual transformer
  }

  @Test
  def doesNotUpdateTransformerWhenNewApplicationHasEmptyTransformer: Unit = {
    //Given
    val holder = SynchronizedHolder(Transformer.empty)
    val transformer = mock[Transformer]
    val oldApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes, holder) {
      override def processDeltas(translator: Translator[Transformer], transformType: TransformType, configDir: URL): Try[Transformer] = Try {
        transformer
      }
    }

    //And
    val newApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes) {
      override def processDeltas(translator: Translator[Transformer], transformType: TransformType, configDir: URL): Try[Transformer] = Try {
        Transformer.empty
      }
    }

    //When
    oldApplication.update(newApplication)

    //Then
    holder.get mustEqual transformer
  }

  @Test
  def addsOneNewNodeFromParsedApplication: Unit = {
    //Given
    val newIp = "127.0.0.11"
    val newNodeIp = InetAddress.getByName(newIp)
    val newNode = new Node("newNode", newNodeIp, ChangeSet(1))
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes + newNode)

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(newNodeIp) mustEqual Some(newNode)
  }

  @Test
  def addsTwoOrMoreNewNodesFromParsedApplication: Unit = {
    //Given
    val newIp1 = "127.0.0.11"
    val newIp2 = "127.0.0.12"
    val newNodeIp1 = InetAddress.getByName(newIp1)
    val newNodeIp2 = InetAddress.getByName(newIp2)
    val newNode1 = new Node("newNode1", newNodeIp1, ChangeSet(1))
    val newNode2 = new Node("newNode2", newNodeIp2, ChangeSet(1))
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes + newNode1 + newNode2)

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(newNodeIp1) mustEqual Some(newNode1)
    application.getNode(newNodeIp2) mustEqual Some(newNode2)
  }

  @Test
  def removesOneOldNodeWhenNewApplicationDoesNotHaveIt: Unit = {
    //Given
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, Set(node1))

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(node1.ip) mustEqual Some(node1)
    application.getNode(node2.ip) mustEqual None
  }

  @Test
  def removesAllOldNodesWhenApplicationHasNone: Unit = {
    //Given
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, Set())

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(node1.ip) mustEqual None
    application.getNode(node2.ip) mustEqual None
  }

  @Test
  def addsANewNodeAndRemovesAnOldNodeWhenApplicationUpdates: Unit = {
    //Given
    val newIp = "127.0.0.11"
    val newNodeIp = InetAddress.getByName(newIp)
    val newNode = new Node("newNode", newNodeIp, ChangeSet(12))
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, Set(node1, newNode))

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(node1.ip) mustEqual Some(node1)
    application.getNode(node2.ip) mustEqual None
    application.getNode(newNodeIp) mustEqual Some(newNode)
  }


  @Test
  def doesNotUpdateApplicationNodesWhenParsedApplicationNodesDidNotChange: Unit = {
    //Given
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes)

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(node1.ip) mustEqual Some(node1)
    application.getNode(node2.ip) mustEqual Some(node2)
  }

  @Test
  def updatesApplicationNodesWhenParsedApplicationNodeNameChanged: Unit = {
    //Given
    val newNode1Name = "newNode1Name"
    val newNode1 = new Node(newNode1Name, node1Ip, ChangeSet(changeSet1))
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, Set(newNode1, node2))

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(node1.ip) match {
      case Some(n) => n.name mustEqual newNode1Name
      case None => failure(s"Should have Node with IP ${node1Ip}")
    }
    application.getNode(node2.ip) mustEqual Some(node2)
  }

  @Test
  def updatesApplicationNodesWhenParsedApplicationNodeChangeSetChanged: Unit = {
    //Given
    val newNode1 = new Node(node1Name, node1Ip, ChangeSet(changeSet2))
    val parsedApplication = new Application(configDir, appName, TransformType.EXPANSION, Set(newNode1, node2))

    //When
    application.update(parsedApplication)

    //Then
    application.getNode(node1.ip) match {
      case Some(n) => n.changeSet mustEqual ChangeSet(2)
      case None => failure(s"Should have Node with IP ${node1Ip}")
    }
    application.getNode(node2.ip) mustEqual Some(node2)
  }

  @Test
  def toStringReturnsParseableApplication: Unit = {
    //Given
    val anApplication = new Application(configDir, appName, TransformType.EXPANSION, nodes)
    val input = new StringReader(anApplication.toString)

    //When-Then
    new ApplicationParsers {
      parseAll(app(configDir), input) match {
        case NoSuccess(msg, _) => throw new AssertionError(msg)
        case Success(application, _) => application mustEqual anApplication
      }
    }
  }
}

object ApplicationSpecs {

  val servers: ServerSetup = new ServerSetup

  @BeforeClass
  def setup() {
    servers.start
  }

  @AfterClass
  def cleanup() {
    servers.stop
  }
}
