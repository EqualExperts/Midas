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

    val node1 = Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val node2 = Node(node2Name, node2Ip, ChangeSet(changeSet2))
    val nodes = List(node1, node2)
    val configDir: URL = new File("src/test/scala/com/ee/midas/myDeltas/myApp").toURI.toURL
    val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/01_add.delta")
    deltaFile.createNewFile()
    val expansionDelta = new PrintWriter(deltaFile)

    expansionDelta.write("use someDatabase\n")
    expansionDelta.write("db.collectionName.add(\'{\"newField\": \"newValue\"}\')")
    expansionDelta.flush()
    expansionDelta.close()
    deltaFile.deleteOnExit()
    val application = Application(configDir, appName, TransformType.EXPANSION, nodes)
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

    "Get Change Set by IP" in new Setup {
      //When
      val actualChangeSet = application.changeSet(node1Ip)

      //Then
      actualChangeSet mustEqual Some(ChangeSet(changeSet1))
    }

    "Give no result if Change Set for the IP is not present" in new Setup {
      //Given
      val ip = InetAddress.getByName("127.0.0.10")

      //When-Then
      application.changeSet(ip) mustEqual None
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

    "Do not transform Request document for invalid IP" in new Setup {
       //Given
       val ip = InetAddress.getByName("127.0.0.6")
       val document = new BasicBSONObject("name", "midas")

       //When
       val transformedDocument: BSONObject = application.transformRequest(document, "validCollection", ip)

       //Then
       transformedDocument mustEqual document
    }

   /* "transform Request document for valid IP" in new Setup {
      //Given
      val document = new BasicBSONObject("name", "midas")

      //When
      val transformedDocument: BSONObject = application.transformRequest(document, "collectionName", node1Ip)

      //Then
      transformedDocument.get("_expansionVersion") mustEqual 1d
    }

     "transform Response document for valid Collection Name" in new Setup {
      //Given
      val document = new BasicBSONObject("name", "midas")

      //When
      val transformedDocument: BSONObject = application.transformResponse(document, "collectionName")

      //Then
      transformedDocument.get("newField") mustEqual "newValue"
    }
*/
    "Do not transform Response document for invalid Collection Name" in new Setup {
      //Given
      val document = new BasicBSONObject("name", "midas")

      //When
      val transformedDocument: BSONObject = application.transformResponse(document, "invalidCollection")

      //Then
      transformedDocument mustEqual document
    }
  }
}
