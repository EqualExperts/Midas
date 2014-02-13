package com.ee.midas.transform

trait RequestTransforms {
  type ChangeSetCollectionKey = (Long, String)
  var requestExpansions: Map[ChangeSetCollectionKey, Double]
  var requestContractions: Map[ChangeSetCollectionKey, Double]

  def canTransformRequest(fullCollectionName: String): Boolean = false
}
