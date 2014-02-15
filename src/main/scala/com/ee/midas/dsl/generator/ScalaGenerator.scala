package com.ee.midas.dsl.generator

import com.ee.midas.transform.{TransformType, Transforms}
import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.utils.Loggable
import com.ee.midas.dsl.grammar.Verb
import scala.collection.JavaConverters._
import org.bson.BSONObject
import scala.collection.immutable.TreeMap
import com.ee.midas.transform.TransformType._

class ScalaGenerator extends Generator[Transforms] with Loggable with SnippetProvider {
  type JDouble = java.lang.Double
  type JLong = java.lang.Long
  type JList[String] = java.util.List[String]
  type MutableVersionedMap = scala.collection.mutable.Map[JDouble, (Verb, JList[String], JLong)]

  def generate(transformType: TransformType, tree: Tree): Transforms = transformType match {
    case EXPANSION =>  new Transforms {
      var responseExpansions: Map[String, VersionedSnippets] = generateResponseSnippets(EXPANSION, tree)
      var responseContractions: Map[String, VersionedSnippets] = Map()
      var transformType: TransformType = EXPANSION
      var requestExpansions: Map[ChangeSetCollectionKey, Double] = generateRequestTransforms(EXPANSION, tree)
      var requestContractions: Map[ChangeSetCollectionKey, Double] = Map()
    }

    case CONTRACTION => new Transforms {
      var responseExpansions: Map[String, VersionedSnippets] = Map()
      var responseContractions: Map[String, VersionedSnippets] = generateResponseSnippets(CONTRACTION, tree)
      var transformType: TransformType = CONTRACTION
      var requestExpansions: Map[ChangeSetCollectionKey, Double] = generateRequestTransforms(EXPANSION, tree)
      var requestContractions: Map[ChangeSetCollectionKey, Double] = generateRequestTransforms(CONTRACTION, tree)
    }
  }

  private def fullCollectionName(dbName: String, collectionName: String) =
    s"$dbName.$collectionName"

  private def generateRequestTransforms(transformType: TransformType, tree: Tree):
  Map[(Long, String), Double] = {
    logInfo(s"Started Request Transforms generation for $transformType TransformType...")
    val requestTransforms = scala.collection.mutable.Map[(Long, String), Double]()
    tree.foreachDelta(transformType, { case (dbName: String, collectionName: String, versionedMap: MutableVersionedMap) =>
      val changeSetTransforms = versionedMap.map { case(version, (_, _, changeSet)) =>
        (changeSet.toLong, fullCollectionName(dbName, collectionName)) -> version.toDouble
      }
      requestTransforms ++= changeSetTransforms
      logDebug(s"Request Transforms = $changeSetTransforms")
    })
    logInfo(s"Completed Request Transforms generation for $transformType TransformType...")
    requestTransforms.toMap
  }

  private def generateResponseSnippets(transformType: TransformType, tree: Tree):
  Map[String, TreeMap[Double, BSONObject=>BSONObject]] = {
    logInfo(s"Started Response Snippets generation for $transformType TransformType...")
    val responseSnippets = scala.collection.mutable.Map[String, TreeMap[Double, BSONObject => BSONObject]]()
    tree.foreachDelta(transformType, { case (dbName: String, collectionName: String, versionedMap: MutableVersionedMap) =>
      val versionedSnippets = versionedMap.map { case(version, (verb, arguments, _)) =>
        val args = arguments.asScala.toArray.collect {
          case arg: String => arg
        }
        (version.toDouble -> toSnippet(verb, args))
      }
      val treeMap = scala.collection.immutable.TreeMap(versionedSnippets.toArray: _*)
      responseSnippets += (fullCollectionName(dbName, collectionName) -> treeMap)
      logDebug(s"Response Snippets = $responseSnippets")
    })
    logInfo(s"Completed Response Snippets generation for $transformType TransformType...")
    responseSnippets.toMap
  }
}
