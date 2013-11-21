package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.BSONObject
import com.mongodb.{DBDecoder, DefaultDBDecoder}

object DocumentConverter {

  private val decoder: DBDecoder = new DefaultDBDecoder()

  def toDocument(src: InputStream): BSONObject = decoder.decode(src, null)
}
