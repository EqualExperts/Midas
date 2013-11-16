package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.{BSONEncoder, BSONObject, BasicBSONEncoder}
import com.mongodb.{DBCollection, DBDecoder, DefaultDBDecoder}

object DocumentConverter {

  private val decoder: DBDecoder = new DefaultDBDecoder()

  def toDocument(src: InputStream): BSONObject = decoder.decode(src, null)
}
