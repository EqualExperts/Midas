package com.ee.midas.interceptor

import java.io.{OutputStream, InputStream}
import org.bson.BSONObject
import DocumentConverter._
import com.ee.midas.pipes.Interceptable


class Interceptor (inputStream: InputStream, dst: OutputStream) {

  private var stop: Boolean  = false

  def read(): Array[Byte] = {
    val header: MongoHeader = MongoHeader(inputStream)
    if(header.hasPayload) {
        val documents = extractDocumentsFrom(inputStream, header)
        val enrichedDocuments = documents map Transformer.transform
        constructResponseUsing(enrichedDocuments, header)
      }
      else header.bytes
  }

  private def constructResponseUsing(enrichedDocuments: List[BSONObject], header: MongoHeader): Array[Byte] = {
    val payloadBytes = enrichedDocuments flatMap toBytes
    header.updateLength(payloadBytes.length)
    header.bytes ++ payloadBytes
  }

  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader ): List[BSONObject] = {
    val stream = new FixedSizeStream(inputStream, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map (n => toDocument(stream))
    documents.toList
  }

  def start: Int = {
      val response = read()
      write(response)
  }

  def stopInterceptor = stop = true

  private def write(data: Array[Byte]): Int = {
    val bytesToWrite = data.length
    dst.write(data, 0, bytesToWrite)
    dst.flush()
    bytesToWrite
  }
}

object Interceptor extends Interceptable {
   def apply(): (InputStream, OutputStream) => Int = intercept

  override def intercept(src: InputStream, tgt: OutputStream): Int = {
    new Interceptor(src, tgt).start
  }

}

