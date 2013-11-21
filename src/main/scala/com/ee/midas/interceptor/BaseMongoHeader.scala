package com.ee.midas.interceptor

import org.bson.io.Bits
import java.nio.{ByteOrder, ByteBuffer}
import java.io.InputStream
import com.ee.midas.interceptor.BaseMongoHeader.OpCode

class BaseMongoHeader(val bytes : Array[Byte]) {
  private val MAX_MESSAGE_LENGTH : Int = ( 32 * 1024 * 1024 )   //change this to response length later
  protected var pos = 0
  private var messageLength = Bits.readInt(bytes, pos)
  pos += 4

  if (messageLength > MAX_MESSAGE_LENGTH) {
    throw new IllegalArgumentException("response too long: " + messageLength)
  }

  val requestID = Bits.readInt(bytes, pos)
  pos += 4

  val responseTo = Bits.readInt(bytes, pos)
  pos += 4

  val opCode = OpCode(Bits.readInt(bytes, pos))
  pos += 4

  def payloadSize: Int = messageLength - bytes.length

  def hasPayload: Boolean = payloadSize > 0

  def length: Int = messageLength

  def updateLength(newLength : Int): Int = {
    messageLength = MongoHeader.SIZE + newLength
    val newLengthBytes = asFourBytes(messageLength)
    for (i <- 0 until 4) {
      bytes(i) = newLengthBytes(i)
    }
    messageLength
  }

  private def asFourBytes(value: Int): Array[Byte] =
    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
}

object BaseMongoHeader {
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

  val SIZE = 16

  def apply(src: InputStream) = {
    val headerBuf = new Array[Byte](SIZE)
    src.read(headerBuf, 0, SIZE)
    new BaseMongoHeader(headerBuf)
  }
}