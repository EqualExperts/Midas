package com.ee.midas.transform

import org.bson.BSONObject
import com.ee.midas.hotdeploy.{Deployable, DeployableHolder}

class Transformer(deployableHolder: DeployableHolder[Transforms]) {

  def transforms: Transforms = deployableHolder.get

  def canTransformResponse(fullCollectionName: String): Boolean =
    transforms.canTransformResponse(fullCollectionName)

  def transformResponse(document: BSONObject, fullCollName: String): BSONObject = {
    implicit val fullCollectionName = fullCollName
    transforms.transformResponse(document, fullCollectionName)
  }

  def canTransformRequest(fullCollectionName: String): Boolean = {
    transforms.canTransformRequest(fullCollectionName)
  }
}
