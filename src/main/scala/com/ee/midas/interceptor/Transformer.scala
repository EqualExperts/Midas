package com.ee.midas.interceptor

import org.bson.BSONObject
import com.ee.midas.transform.Transformations._
import com.ee.midas.transform.TransformType

object Transformer {

  def canTransformDocuments(implicit fullCollectionName: String): Boolean =
    canBeApplied(fullCollectionName)

  def transform(document: BSONObject)(implicit fullCollectionName: String): BSONObject =
    map(document)(fullCollectionName, TransformType.EXPANSION)
}
