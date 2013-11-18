package com.ee.midas.interceptor

import java.io.{OutputStream, InputStream}
import org.bson.BSONObject
import DocumentConverter._
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.pipes.Interceptable


class ResponseInterceptor private extends Interceptable {

  private def read(inputStream: InputStream): Array[Byte] = {
    val header: MongoHeader = MongoHeader(inputStream)
    if(header.hasPayload) extractPayload(inputStream, header) else header.bytes
  }

  private def isHandshakeDocument(document: BSONObject): Boolean =
    ((document containsField "you") || (document containsField "ok"))


  private def constructResponseUsing(documents: List[BSONObject], header: MongoHeader): Array[Byte] = {
    val payloadBytes = documents flatMap (_.toBytes)
    header.updateLength(payloadBytes.length)
    header.bytes ++ payloadBytes
  }

  import MongoHeader.OpCode._
  private def extractPayload(in: InputStream, header: MongoHeader) = {
    val documents = extractDocumentsFrom(in, header)
    val transformedDocuments = documents map Transformer.transform
    header.opCode match {
      case OP_REPLY => println(s"RESPONSE OP_REPLY ***** $documents")
      case opCode => println(s"RESPONSE $opCode")
    }
    constructResponseUsing(transformedDocuments, header)
  }
  
  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader): List[BSONObject] = {
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

object ResponseInterceptor {
  def apply(): ResponseInterceptor = new ResponseInterceptor()
}

