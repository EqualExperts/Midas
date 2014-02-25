package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import java.net.{Socket, URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.specs2.specification.Scope
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, PrintWriter, File}
import org.specs2.mock.Mockito
import com.ee.midas.dsl.Translator
import scala.util.Try


@RunWith(classOf[JUnitRunner])
class ApplicationSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val appName = "myApp"
    val nonExistentAppName = "nonExistentApp"

    val ip1 = "127.0.0.1"
    val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)

    val ip2 = "127.0.0.0"
    val (node2Name, node2Ip, changeSet2) = ("node2", InetAddress.getByName(ip2), 2)

    val node1 = new Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val node2 = new Node(node2Name, node2Ip, ChangeSet(changeSet2))
    val nodes = Set(node1, node2)
  }

  isolated
  "Application" should {

    "Manage Nodes" in new Setup {
      val configDir: URL = null
      val application = new Application(configDir, appName, TransformType.EXPANSION, nodes)

      "By allowing a Node to be retrieved by IP" in {
        //When-Then
        application.getNode(node2Ip) mustEqual Some(node2)
      }

      "By giving no result if Node with that IP is not present" in {
        //Given
        val ip = InetAddress.getByName("127.0.0.9")

        //When-Then
        application.getNode(ip) mustEqual None
      }

      "By Affirming Node's presence in the application by IP" in {
        //When-Then
        application.hasNode(node2Ip) mustEqual true
      }

      "By Deny Node's presence in the application by IP" in {
        //Given
        val ip = InetAddress.getByName("127.0.0.6")

        //When-Then
        application.hasNode(ip) mustEqual false
      }

      "By Stopping them" in {
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
    }

    "Accepts Connection for a node available in configuration" in new Setup {
      //Given
      val client = mock[Socket]
      client.getInetAddress returns node1Ip
      val mongo = mock[Socket]
      val configDir: URL = null
      val application = new Application(configDir, appName, TransformType.EXPANSION, nodes)
      val data: Array[Byte] = Array(1.toByte)
      client.getInputStream returns new ByteArrayInputStream(data)
      client.getOutputStream returns new ByteArrayOutputStream
      mongo.getInputStream returns new ByteArrayInputStream(data)
      mongo.getOutputStream returns new ByteArrayOutputStream

      //When
      application.acceptAuthorized(client, mongo)

      //Then
      node1.isActive mustEqual true
      node2.isActive mustEqual false
    }

    "Ignores if connection comes from node not available in configuration" in new Setup {
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


    "Parse Deltas when application is created" in new Setup {
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
}
