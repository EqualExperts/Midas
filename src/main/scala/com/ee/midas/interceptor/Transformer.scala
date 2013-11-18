package com.ee.midas.interceptor

import org.bson.BSONObject
import com.ee.midas.transform.Transformations._

object Transformer {

  def transform(document: BSONObject): BSONObject = document
//  def transform(document: BSONObject): BSONObject = map(document)
//  def transform(document: BSONObject): BSONObject =
//    if (document.containsField("_id")) map(document) else document

}
