package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.io.Bits

class MongoHeader private(override val bytes: Array[Byte]) extends BaseMongoHeader(bytes.take(16)) {
  val flags = Bits.readInt(bytes, pos + (4 * 4))   //Int = 4 bytes
  val cursor = Bits.readLong(bytes, pos + (4 * 5))
  val startingFrom = Bits.readInt(bytes, pos + (4 * 7)) //Long = 8 bytes
  val documentsCount = Bits.readInt(bytes, pos + (4 * 8))
}

object MongoHeader {
  val SIZE = BaseMongoHeader.SIZE + 20

  def apply(src: InputStream) = {
    val headerBuf = new Array[Byte](SIZE)
    src.read(headerBuf, 0, SIZE)
    new MongoHeader(headerBuf)
  }
}
