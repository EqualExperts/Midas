package com.ee.midas.interceptor

import org.bson.BSONObject
import com.ee.midas.transform.Transformations._

object Transformer {

  def transform(document: BSONObject): BSONObject = map(document)

}
