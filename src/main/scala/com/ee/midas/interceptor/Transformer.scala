package com.ee.midas.interceptor

import org.bson.BSONObject

object Transformer {

  def transform(dbObject: BSONObject): BSONObject = {
    dbObject.put("version", "1.0")
    dbObject
  }

}
