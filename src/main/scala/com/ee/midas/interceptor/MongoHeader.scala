package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.io.Bits

class MongoHeader private(override val bytes: Array[Byte]) extends BaseMongoHeader(bytes.take(16)) {
  val flags = Bits.readInt(bytes, pos)
  pos += 4

  val cursor = Bits.readLong(bytes, pos)
  pos += 8

  val startingFrom = Bits.readInt(bytes, pos)
  pos += 4

  val documentsCount = Bits.readInt(bytes, pos)
}

object MongoHeader {
  val SIZE = BaseMongoHeader.SIZE + 20

  def apply(src: InputStream) = {
    val headerBuf = new Array[Byte](SIZE)
    src.read(headerBuf, 0, SIZE)
    new MongoHeader(headerBuf)
  }
}
