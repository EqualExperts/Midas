package com.ee.midas.interceptor

import org.bson.BSONObject
import com.ee.midas.hotdeploy.DeployableHolder

import com.ee.midas.transform.{Transforms, TransformType}

class Transformer(val transformType: TransformType, deployableHolder: DeployableHolder[Transforms]) {
  def canTransformDocuments(implicit fullCollectionName: String): Boolean =
    deployableHolder.get.canBeApplied(fullCollectionName)

  def transform(document: BSONObject)(implicit fullCollectionName: String): BSONObject =
    deployableHolder.get.map(document)(fullCollectionName, transformType)
}
