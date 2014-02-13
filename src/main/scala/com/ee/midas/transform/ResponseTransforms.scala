package com.ee.midas.transform

import org.bson.BSONObject
import scala.collection.immutable.TreeMap
import TransformType._

trait ResponseTransforms extends Versioner {
  type Snippet = BSONObject => BSONObject
  type Snippets = Iterable[Snippet]
  type VersionedSnippets = TreeMap[Double, Snippet]
  var responseExpansions: Map[String, VersionedSnippets]
  var responseContractions: Map[String, VersionedSnippets]

  var transformType: TransformType

  def canTransformResponse(fullCollectionName: String): Boolean =
    responseExpansions.keySet.contains(fullCollectionName) || responseContractions.keySet.contains(fullCollectionName)

  def transformResponse(document: BSONObject, fullCollectionName: String) : BSONObject =  {
    versionedSnippets(fullCollectionName) match {
      case map if map.isEmpty => document
      case vs =>
        val version = getVersion(document)(transformType) match {
          case Some(version) => version + 1
          case None => 1
        }
        val snippets = snippetsFrom(version, vs)
        applySnippets(snippets, document)
    }
  }

  def versionedSnippets(fullCollectionName: String): VersionedSnippets =
    if(transformType == EXPANSION)
      responseExpansions(fullCollectionName)
    else if(transformType == CONTRACTION)
      responseContractions(fullCollectionName)
    else TreeMap.empty

  def snippetsFrom(version: Double, versionedSnippets: VersionedSnippets) =
    versionedSnippets.filterKeys(v => v >= version).unzip._2

  def applySnippets(snippets: Snippets, document: BSONObject): BSONObject =
    snippets.foldLeft(document) {
      case (document, snippet) => (snippet andThen versionDocument)(document)
    }

  def versionDocument(document: BSONObject): BSONObject = version(document)(transformType)
}

