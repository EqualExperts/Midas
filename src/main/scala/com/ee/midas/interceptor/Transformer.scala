package com.ee.midas.interceptor

import org.bson.BSONObject
//import com.ee.midas.transform.Transformations._
import com.ee.midas.transform.{TransformsHolder, TransformType}

class Transformer {

  def canTransformDocuments(implicit fullCollectionName: String): Boolean =
    TransformsHolder.get.canBeApplied(fullCollectionName)

  def transform(document: BSONObject)(implicit fullCollectionName: String): BSONObject =
    //TODO: let the implicit come from midas mode
    TransformsHolder.get.map(document)(fullCollectionName, TransformType.EXPANSION)
}
