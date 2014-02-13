package com.ee.midas.transform

import org.bson.BSONObject
import com.ee.midas.hotdeploy.DeployableHolder

class Transformer(deployableHolder: DeployableHolder[Transforms]) {
  def canTransformResponse(fullCollectionName: String): Boolean =
    deployableHolder.get.canBeApplied(fullCollectionName)

  def transformResponse(document: BSONObject, fullCollName: String): BSONObject = {
    implicit val fullCollectionName = fullCollName
    deployableHolder.get.map(document)(fullCollectionName)
  }

  def canTransformRequest(fullCollectionName: String): Boolean = false
}
