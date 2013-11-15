package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.{BSONEncoder, BSONObject, BasicBSONEncoder}
import com.mongodb.{DBCollection, DBDecoder, DefaultDBDecoder}

object DocumentConverter {

  private val encoder: BSONEncoder = new BasicBSONEncoder()
  private val decoder: DBDecoder = new DefaultDBDecoder()
  private val collection: DBCollection = null

  def toBytes(document: BSONObject): Array[Byte] = encoder.encode(document)

  def toDocument(src: InputStream): BSONObject = decoder.decode(src, collection)
}
