package com.ee.midas.pipes

import java.io.InputStream
import com.ee.midas.applier.{Applier, MongoHeader}

class DecoratorStream(inputStream: InputStream) extends InputStream {

  override def read(): Int = {
    println("DecoratorStream: read() called")
    inputStream.read()
  }

  override def read(buffer: Array[Byte]): Int = {
    println("DecoratorStream: read(buffer: Array[Byte]) called")
    val mongoHeader: MongoHeader = readHeader(inputStream)
    val midasInputStream = new MyInputStream(inputStream, mongoHeader.getResponseLength - MONGO_HEADER_LENGTH)
    val data: Array[Byte] = new Applier().applySchema(midasInputStream, mongoHeader)
    System.arraycopy(data, 0, buffer, 0, data.length)
    data.length
  }

  val MONGO_HEADER_LENGTH = 36

  def readHeader(stream: InputStream): MongoHeader = {
    val header = new Array[Byte](MONGO_HEADER_LENGTH)
    stream.read(header, 0, MONGO_HEADER_LENGTH)
    new MongoHeader(header)
  }

}

object DecoratorStream {
  def apply(inputStream: InputStream): InputStream = {
    new DecoratorStream(inputStream)
  }
}
