package com.ee.midas.applier

import org.bson.io.Bits
import java.nio.{ByteOrder, ByteBuffer}


class MongoHeader {
  var responseLength : Int = 0
  var id : Int = 0
  var responseTo : Int = 0
  var operation : Int = 0
  var flags : Int = 0
  var cursor : Long = 0
  var startingFrom : Int = 0
  var numOfDocuments : Int  = 0
  val MAX_LENGTH = ( 32 * 1024 * 1024 )
  var array : Array[Byte] = null

  def MongoHeader(header: Array[Byte]) {

    this.array = header

    var pos : Int = 0

    responseLength = Bits.readInt(header, pos)
    pos += 4

    if (responseLength > MAX_LENGTH) {
      throw new IllegalArgumentException("response too long: "
        + responseLength)
    }

    id = Bits.readInt(header, pos)
    pos += 4

    responseTo = Bits.readInt(header, pos)
    pos += 4

    operation = Bits.readInt(header, pos)
    pos += 4

    flags = Bits.readInt(header, pos)
    pos += 4

    cursor = Bits.readLong(header, pos)
    pos += 8

    startingFrom = Bits.readInt(header, pos)
    pos += 4

    numOfDocuments = Bits.readInt(header, pos)
    pos += 4

  }

  def getResponseLength: Int = responseLength

  def getNumOfDocuments: Int = numOfDocuments

  def getArray: Array[Byte] = array

  def updateLength(newLength : Int) : Unit = {
    val totalSize = 36 + newLength
    val newLengthBytes = ByteBuffer.allocate(4)
      .order(ByteOrder.LITTLE_ENDIAN).putInt(totalSize).array()
    for ( i <- 0 to  3) {
      array(i) = newLengthBytes(i)
    }
    responseLength=totalSize
  }
}
