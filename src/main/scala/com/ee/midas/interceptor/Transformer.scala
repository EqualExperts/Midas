package com.ee.midas.interceptor

import org.bson.BSONObject
import com.ee.midas.transform.Transformations._

object Transformer {

  def canTransform(collectionName: String): Boolean = false

  def transform(document: BSONObject): BSONObject = map(document)
}
