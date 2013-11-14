package com.ee.midas.interceptor

import java.io.InputStream
import java.nio.{ByteBuffer,ByteOrder}
import org.bson.io.Bits

class MongoHeader private(var bytes: Array[Byte]) {

  private var pos = 0

  private var responseLength = Bits.readInt(bytes, pos)
  pos += 4

  if (responseLength > MongoHeader.MAX_LENGTH) {
    throw new IllegalArgumentException("response too long: " + responseLength)
  }

  pos += 28

  val documentsCount = Bits.readInt(bytes, pos)
  pos += 4

  def payloadSize: Int = responseLength - MongoHeader.SIZE

  def hasPayload: Boolean = payloadSize > 0

  def updateLength(newLength : Int): Int = {
    responseLength = MongoHeader.SIZE + newLength
    val newLengthBytes = asFourBytes(responseLength)
    for (i <- 0 until 4) {
      bytes(i) = newLengthBytes(i)
    }
    responseLength
  }

  private def asFourBytes(value: Int): Array[Byte] =
    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

}


object MongoHeader {
  private val MAX_LENGTH : Int = ( 32 * 1024 * 1024 )   //change this to response length later
  val SIZE = 36
  def apply(src: InputStream) = {
    val headerBuf = new Array[Byte](SIZE)
    src.read(headerBuf, 0, SIZE)
    new MongoHeader(headerBuf)
  }
}
