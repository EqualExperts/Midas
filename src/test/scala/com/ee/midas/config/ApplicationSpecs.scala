package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{Specification}
import java.net.{Socket, URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.specs2.specification.Scope
import org.bson.{BSONObject, BasicBSONObject}
import scala.collection.immutable.TreeMap
import java.io.{PrintWriter, File}
import org.specs2.mock.Mockito

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

//    "Accepts Connection for a node available in configuration" in new Setup {
//      //Given
//      val configDir: URL = null
//      val n1 = mock[Node]
//      n1.ip returns node1Ip
//
//      val n2 = mock[Node]
//      n2.ip returns node2Ip
//
//      val client = mock[Socket]
//      client.getInetAddress returns node1Ip
//
//      val mongo = mock[Socket]
//      val application = new Application(configDir, appName, TransformType.EXPANSION, Set(n1, n2))
//
//
//      //When
//      application.acceptAuthorized(client, mongo)
//
//      //Then
//      there was one(n1).startDuplexPipe(client, mongo, Transformer.empty)
//      there was no(n2).startDuplexPipe(client, mongo, Transformer.empty)
//    }


    "Parse Deltas" in new Setup {
      val configDir: URL = new File("src/test/scala/com/ee/midas/myDeltas/myApp").toURI.toURL
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/01_add.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase\n")
      expansionDelta.write("db.collectionName.add(\'{\"newField\": \"newValue\"}\')")
      expansionDelta.flush()
      expansionDelta.close()
      deltaFile.deleteOnExit()

//      "" in {
//
//      }
    }

  }
}
