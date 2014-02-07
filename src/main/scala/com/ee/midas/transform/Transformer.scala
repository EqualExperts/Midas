package com.ee.midas.transform

import org.bson.BSONObject
import com.ee.midas.hotdeploy.DeployableHolder

class Transformer(deployableHolder: DeployableHolder[Transforms]) {
  def canTransformDocuments(implicit fullCollectionName: String): Boolean =
    deployableHolder.get.canBeApplied(fullCollectionName)

  def transform(document: BSONObject)(implicit fullCollectionName: String): BSONObject =
    deployableHolder.get.map(document)(fullCollectionName)
}
