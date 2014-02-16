package com.ee.midas.transform

import org.bson.BSONObject
import scala.collection.immutable.TreeMap
import TransformType._

trait ResponseTransforms extends ResponseTypes with ResponseVersioner {
  var responseExpansions: Map[String, VersionedSnippets]
  var responseContractions: Map[String, VersionedSnippets]

  var transformType: TransformType

  def transformResponse(document: BSONObject, fullCollectionName: String) : BSONObject =  {
    transformType match {
      case EXPANSION => {
         if(responseExpansions.keySet.contains(fullCollectionName))
            transform(document, fullCollectionName)
         document
      }
      case CONTRACTION => {
        if(responseContractions.keySet.contains(fullCollectionName))
          transform(document, fullCollectionName)
        document
      }
    }

  }

  def transform(document: BSONObject, fullCollectionName: String) : BSONObject = {
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

