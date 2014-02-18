package com.ee.midas.transform

import com.ee.midas.config.{ChangeSet}
import TransformType._
import org.bson.BSONObject

trait RequestTransformer extends RequestTypes with RequestVersioner {
  var requestExpansions: Map[ChangeSetCollectionKey, Double]
  var requestContractions: Map[ChangeSetCollectionKey, Double]

  var transformType: TransformType

  def transformRequest(document: BSONObject, changeSet: Long, fullCollectionName: String): BSONObject = {
    val key = (changeSet, fullCollectionName)
    transformType match {
      case EXPANSION => {
        if(requestExpansions.isDefinedAt(key)) {
          val version = requestExpansions(key)
          addExpansionVersion(document, version)
        }
        document
      }

      case CONTRACTION => {
        if (requestExpansions.isDefinedAt(key)) {
          val expansionVersion = requestExpansions(key)
          addExpansionVersion(document, expansionVersion)
        }
        if (requestContractions.isDefinedAt(key)) {
          val contractionVersion = requestContractions(key)
          addContractionVersion(document, contractionVersion)
        }
        document
      }
    }
  }
}
