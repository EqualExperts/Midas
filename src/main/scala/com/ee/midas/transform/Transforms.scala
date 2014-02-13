package com.ee.midas.transform

import org.bson.BSONObject
import TransformType._
import com.ee.midas.hotdeploy.Deployable
import scala.collection.immutable.TreeMap

abstract class Transforms extends Versioner with Deployable[Transforms] {
  type Snippet = BSONObject => BSONObject
  type Snippets = Iterable[Snippet]
  type VersionedSnippets = TreeMap[Double, Snippet]
  var responseExpansions: Map[String, VersionedSnippets]
  var responseContractions: Map[String, VersionedSnippets]
  
  type ChangeSetCollectionKey = (Long, String)
  var requestExpansions: Map[ChangeSetCollectionKey, Double]
  var requestContractions: Map[ChangeSetCollectionKey, Double]
  
  implicit var transformType: TransformType

  def injectState(fromTransforms: Transforms) = {
    this.responseExpansions = fromTransforms.responseExpansions
    this.responseContractions = fromTransforms.responseContractions
    this.requestExpansions = fromTransforms.requestExpansions
    this.requestContractions = fromTransforms.requestContractions
    this.transformType = fromTransforms.transformType
  }

  def canBeApplied(fullCollectionName: String): Boolean =
    responseExpansions.keySet.contains(fullCollectionName) || responseContractions.keySet.contains(fullCollectionName)

  def map(document: BSONObject)(implicit fullCollectionName: String) : BSONObject =  {
    versionedSnippets match {
      case map if map.isEmpty => document
      case vs =>
        val version = getVersion(document) match {
          case Some(version) => version + 1
          case None => 1
        }
        val snippets = snippetsFrom(version, vs)
        applySnippets(snippets, document)
    }
  }

  def versionedSnippets(implicit fullCollectionName: String): VersionedSnippets =
    if(transformType == EXPANSION)
      responseExpansions(fullCollectionName)
    else if(transformType == CONTRACTION)
      responseContractions(fullCollectionName)
    else TreeMap.empty

  def snippetsFrom(version: Double, versionedSnippets: VersionedSnippets) =
    versionedSnippets.filterKeys(v => v >= version).unzip._2

  def applySnippets(snippets: Snippets, document: BSONObject): BSONObject =
    snippets.foldLeft(document) {
      case (document, snippet) => (snippet andThen version)(document)
    }

  override def toString = {
    val separator = "\n\t\t\t\t\t\t"
    s"""
      |======================================================================
      |Request
      | |
      | +--> Expansions = ${requestExpansions.size} [${requestExpansions mkString separator}]
      | |
      | +--> Contractions = ${requestContractions.size} [${requestContractions mkString separator}]
      |----------------------------------------------------------------------
      |Response
      | |
      | +--> Expansions = ${responseExpansions.size} [${responseExpansions mkString separator}]
      | |
      | +--> Contractions = ${responseContractions.size} [${responseContractions mkString separator}]
      |======================================================================
     """.stripMargin
  }
}
