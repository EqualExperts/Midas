package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{Specification}
import java.net.{URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.specs2.specification.Scope
import org.bson.{BSONObject, BasicBSONObject}
import com.ee.midas.transform.ResponseTypes
import org.specs2.execute.Snippet
import scala.collection.immutable.TreeMap
import com.mongodb.util.JSON

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

    "Do not transform Request document for invalid IP" in {
       //Given
       val newTransformer = new Transformer {
          var transformType = TransformType.EXPANSION
          var responseExpansions : Map[String, VersionedSnippets] = Map()
          var responseContractions : Map[String, VersionedSnippets] = Map()

          var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 4d))
          var requestContractions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 2d))
       }
       val ip = InetAddress.getByName("127.0.0.6")
       val document = new BasicBSONObject("name", "midas")

       //When
       application.transformer = newTransformer
       val transformedDocument: BSONObject = application.transformRequest(document, "validCollection", ip)

       //Then
       transformedDocument mustEqual document
    }

    "transform Request document for valid IP" in {
      //Given
      val newTransformer = new Transformer {
        var transformType = TransformType.EXPANSION
        var responseExpansions : Map[String, VersionedSnippets] = Map()
        var responseContractions : Map[String, VersionedSnippets] = Map()

        var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 4d))
        var requestContractions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 2d))
      }
      val ip = InetAddress.getByName("127.0.0.1")
      val document = new BasicBSONObject("name", "midas")

      //When
      application.transformer = newTransformer
      val transformedDocument: BSONObject = application.transformRequest(document, "validCollection", ip)

      //Then
      transformedDocument.get("_expansionVersion") mustEqual 4d
    }

    "Do not transform Response document" in {
      //Given
      val newTransformer = new Transformer {
        var transformType = TransformType.EXPANSION
        var responseExpansions : Map[String, VersionedSnippets] =
          Map(("validCollection", TreeMap(1d ->
            ((document: BSONObject) => {
              document.put("newField", "newValue")
              document
            }))
          ))
        var responseContractions : Map[String, VersionedSnippets] = Map()

        var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map()
        var requestContractions: Map[ChangeSetCollectionKey, Double] = Map()
      }
      val document = new BasicBSONObject("name", "midas")

      //When
      application.transformer = newTransformer
      val transformedDocument: BSONObject = application.transformResponse(document, "validCollection")

      //Then
      transformedDocument.get("newField") mustEqual "newValue"
    }
  }
}
