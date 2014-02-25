package com.ee.midas.config

import org.junit.runner.RunWith
import java.net.{ServerSocket, Socket, URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.specs2.mock.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.{AfterClass, BeforeClass, Test}
import com.ee.midas.dsl.Translator
import java.io.File
import scala.util.Try

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

  val appName = "myApp"
  val nonExistentAppName = "nonExistentApp"

  val ip1 = "127.0.0.1"
  val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)

  val ip2 = "127.0.0.0"
  val (node2Name, node2Ip, changeSet2) = ("node2", InetAddress.getByName(ip2), 2)

  val node1 = new Node(node1Name, node1Ip, ChangeSet(changeSet1))
  val node2 = new Node(node2Name, node2Ip, ChangeSet(changeSet2))
  val nodes = Set(node1, node2)
  val configDir: URL = null
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
    application.hasNode(node2Ip) mustEqual true
  }

  @Test
  def denyNodePresenceByIP() {
    //Given
    val ip = InetAddress.getByName("127.0.0.6")

    //When-Then
    application.hasNode(ip) mustEqual false
  }

  @Test
  def stopNodes() {
    //Given
    val node1 = mock[Node]
    val node2 = mock[Node]
    val application = new Application(configDir, appName, TransformType.EXPANSION, Set(node1, node2))


    //When
    application.stop

    //Then
    there was one(node1).stop
    there was one(node2).stop
  }

  @Test
  def acceptsConnectionForNodeAvailableInConfiguration() {
      //Given
      val host = "localhost"
      val clientSocket = new Socket(host, ServerSetup.appServerPort)
      val mongoSocket = new Socket(host, ServerSetup.mongoServerPort)
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
    val configDir: URL = null
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
    val configDir: URL = new File("/src/test/scala/myDeltas/myApp").toURI.toURL

    //When
    val application = new Application(configDir, appName, TransformType.EXPANSION, nodes) {
     override def processDeltas(translator: Translator[Transformer], transformType: TransformType, configDir: URL): Try[Transformer] = Try {
        processDeltaCalled = 1
        (Transformer.empty)
     }
    }

    //Then
    processDeltaCalled mustEqual 1
  }
}

object ApplicationSpecs {
  @BeforeClass
  def setup() {
    ServerSetup.setUpSockets()
  }

  @AfterClass
  def cleanup() {
    ServerSetup.shutdownSockets()
  }
}
