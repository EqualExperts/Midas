package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{Specification}
import java.net.{URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}

@RunWith(classOf[JUnitRunner])
class ApplicationSpecs extends Specification {

  val appName = "testApp"
  val nonExistentAppName = "nonExistentApp"

  val ip1 = "127.0.0.1"
  val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)

  val ip2 = "127.0.0.0"
  val (node2Name, node2Ip, changeSet2) = ("node2", InetAddress.getByName(ip2), 2)

  val node1 = Node(node1Name, node1Ip, ChangeSet(changeSet1))
  val node2 = Node(node2Name, node2Ip, ChangeSet(changeSet2))
  val nodes = List(node1, node2)
  val ignoreConfigDir: URL = null
  val application = Application(ignoreConfigDir, appName, TransformType.EXPANSION, nodes)

  "Application" should {

    "Get Node by IP" in {
       //When-Then
       application.getNode(node2Ip) mustEqual Some(node2)
    }

    "Give no result if Node with that IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When-Then
      application.getNode(ip) mustEqual None
    }

    "Get Change Set by IP" in {
      //When
      val actualChangeSet = application.changeSet(node1Ip)

      //Then
      actualChangeSet mustEqual Some(ChangeSet(changeSet1))
    }

    "Give no result if Change Set for the IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.10")

      //When-Then
      application.changeSet(ip) mustEqual None
    }

    "Affirm Node's presence in the application by IP" in {
      //When-Then
      application.hasNode(node2Ip) mustEqual true
    }

    "Deny Node's presence in the application by IP" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.6")

      //When-Then
      application.hasNode(ip) mustEqual false
    }

    "be created with empty transforms" in {
      //When-Then
      application.transformer mustEqual Transformer.empty
    }
  }

}
