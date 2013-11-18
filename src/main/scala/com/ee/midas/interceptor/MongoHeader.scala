package com.ee.midas.interceptor

import java.io.InputStream
import java.nio.{ByteBuffer,ByteOrder}
import org.bson.io.Bits
import com.ee.midas.interceptor.MongoHeader.OpCode

class MongoHeader private(var bytes: Array[Byte]) {


  private var pos = 0
  private val MAX_RESPONSE_LENGTH : Int = ( 32 * 1024 * 1024 )   //change this to response length later
  private var responseLength = Bits.readInt(bytes, pos)
  pos += 4

  if (responseLength > MAX_RESPONSE_LENGTH) {
    throw new IllegalArgumentException("response too long: " + responseLength)
  }

  pos += 8
  val opCode = OpCode(Bits.readInt(bytes, pos))

  pos += 20

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
  object OpCode extends Enumeration {
    type OpCode = Value
    val OP_REPLY = Value(1)
    val OP_MSG = Value(1000)
    val OP_UPDATE = Value(2001)
    val OP_INSERT = Value(2002)
    val RESERVED = Value(2003)
    val OP_QUERY = Value(2004)
    val OP_GET_MORE = Value(2005)
    val OP_DELETE = Value(2006)
    val OP_KILL_CURSORS = Value(2007)
  }

  val SIZE = 36
  def apply(src: InputStream) = {
    val headerBuf = new Array[Byte](SIZE)
    src.read(headerBuf, 0, SIZE)
    new MongoHeader(headerBuf)
  }
}
