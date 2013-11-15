package com.ee.midas.interceptor

import java.io.{OutputStream, InputStream}
import org.bson.BSONObject
import DocumentConverter._
import com.ee.midas.pipes.Interceptable


class Interceptor private extends Interceptable {

  private def read(inputStream: InputStream): Array[Byte] = {
    val header: MongoHeader = MongoHeader(inputStream)
    if(header.hasPayload) {
        val documents = extractDocumentsFrom(inputStream, header)
        val enrichedDocuments = documents map Transformer.transform
        constructResponseUsing(enrichedDocuments, header)
      }
      else header.bytes
  }

  private def constructResponseUsing(documents: List[BSONObject], header: MongoHeader): Array[Byte] = {
    val payloadBytes = documents flatMap toBytes
    header.updateLength(payloadBytes.length)
    header.bytes ++ payloadBytes
  }

  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader ): List[BSONObject] = {
    val stream = new FixedSizeStream(inputStream, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map (n => toDocument(stream))
    documents.toList
  }

  override def intercept(src: InputStream, tgt: OutputStream): Int = {
      val response = read(src)
      write(response, tgt)
  }

  private def write(data: Array[Byte], tgt: OutputStream): Int = {
    val bytesToWrite = data.length
    tgt.write(data, 0, bytesToWrite)
    tgt.flush()
    bytesToWrite
  }
}

object Interceptor {
  def apply(): Interceptor = new Interceptor()
}

