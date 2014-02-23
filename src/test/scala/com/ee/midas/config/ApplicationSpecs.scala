package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{Specification}
import java.net.{URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.specs2.specification.Scope
import org.bson.{BSONObject, BasicBSONObject}
import scala.collection.immutable.TreeMap
import java.io.{PrintWriter, File}

@RunWith(classOf[JUnitRunner])
class ApplicationSpecs extends Specification {

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
//    val configDir: URL = new File("src/test/scala/com/ee/midas/myDeltas/myApp").toURI.toURL
    val configDir: URL = null
    val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/01_add.delta")
    deltaFile.createNewFile()
    val expansionDelta = new PrintWriter(deltaFile)

    expansionDelta.write("use someDatabase\n")
    expansionDelta.write("db.collectionName.add(\'{\"newField\": \"newValue\"}\')")
    expansionDelta.flush()
    expansionDelta.close()
    deltaFile.deleteOnExit()
    val application = new Application(configDir, appName, TransformType.EXPANSION, nodes)
  }

  "Application" should {

    "Get Node by IP" in new Setup {
       //When-Then
       application.getNode(node2Ip) mustEqual Some(node2)
    }

    "Give no result if Node with that IP is not present" in new Setup {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When-Then
      application.getNode(ip) mustEqual None
    }

    "Affirm Node's presence in the application by IP" in new Setup {
      //When-Then
      application.hasNode(node2Ip) mustEqual true
    }

    "Deny Node's presence in the application by IP" in new Setup {
      //Given
      val ip = InetAddress.getByName("127.0.0.6")

      //When-Then
      application.hasNode(ip) mustEqual false
    }
  }
}
