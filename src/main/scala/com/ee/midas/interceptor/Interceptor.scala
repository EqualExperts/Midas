package com.ee.midas.interceptor

import java.io.{OutputStream, InputStream}
import org.bson.BSONObject
import com.ee.midas.utils.{Interceptable, OverridingInterceptable}


class Interceptor (inputStream: InputStream, dst: OutputStream) {
  val interpret: Interpreter  = new Interpreter()

  private var stop: Boolean  = false

  def read(): Array[Byte] = {
    val header: MongoHeader = MongoHeader(inputStream)
    header.hasPayload match {
      case true => {
        val documents: List[BSONObject] = extractDocumentsFrom(inputStream, header)
        val enrichedDocuments: List[BSONObject] = documents map Transformer.transform
        constructResponseUsing(enrichedDocuments, header)
      }
      case false => header.bytes
    }
  }

  private def constructResponseUsing(enrichedDocuments: List[BSONObject], header: MongoHeader): Array[Byte] = {
    val payloadBytes = interpret.asBytes(enrichedDocuments)
    header.updateLength(payloadBytes.length)
    header.bytes ++ payloadBytes
  }

  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader ): List[BSONObject] = {
    val stream: InputStream = new FixedSizeStream(inputStream, header.payloadSize)
    interpret.documentsFrom(stream, header.documentsCount)
  }

  def start(): Int = {
      val response = read()
      write(response)
  }

  def stopInterceptor() = stop = true

  private def write(data: Array[Byte]): Int = {
    val bytesToWrite = data.length
    dst.write(data, 0, bytesToWrite)
    dst.flush()
    bytesToWrite
  }
}

object Interceptor extends Interceptable with OverridingInterceptable {
  override def intercept(src: InputStream, tgt: OutputStream): Int = {
    new Interceptor(src, tgt).start()
  }
}

