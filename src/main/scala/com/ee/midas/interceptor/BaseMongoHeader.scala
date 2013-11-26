package com.ee.midas.interceptor

import org.bson.io.Bits
import java.nio.{ByteOrder, ByteBuffer}
import java.io.InputStream
import com.ee.midas.interceptor.BaseMongoHeader.OpCode
import com.ee.midas.utils.Loggable

class BaseMongoHeader(val bytes : Array[Byte]) {
  private val MAX_MESSAGE_LENGTH : Int = ( 32 * 1024 * 1024 )   //change this to response length later
  protected val pos = 0
  private var messageLength = Bits.readInt(bytes, pos)

  if (messageLength > MAX_MESSAGE_LENGTH) {
    throw new IllegalArgumentException("message too long: " + messageLength)
  }

  if(messageLength == 0) {
    throw new IllegalArgumentException("Message Length is ZERO ==> Interpreting it as TerminationMessage")
  }

  val requestID = Bits.readInt(bytes, pos + 4)  //Int = 4 bytes
  val responseTo = Bits.readInt(bytes, pos + (4 * 2))
  val opCode = OpCode(Bits.readInt(bytes, pos + (4 * 3)))

  def payloadSize: Int = messageLength - bytes.length

  def hasPayload: Boolean = payloadSize > 0

  def length: Int = messageLength

  def updateLength(newLength : Int): Int = {
    messageLength = bytes.length + newLength
    val newLengthBytes = asFourBytes(messageLength)
    for (i <- 0 until 4) {
      bytes(i) = newLengthBytes(i)
    }
    messageLength
  }

  private def asFourBytes(value: Int): Array[Byte] =
    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

  override def toString  = {
    "MESSAGE LENGTH " + length + "\n" +
    "PAYLOAD SIZE " + payloadSize + "\n" +
    "REQUEST ID " + requestID + "\n" +
    "RESPONSE TO " + responseTo + "\n" +
    "OPCODE " + opCode
  }
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
