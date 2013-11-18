package com.ee.midas.interceptor

import java.io.{OutputStream, InputStream}
import org.bson.BSONObject
import DocumentConverter._
import com.ee.midas.transform.DocumentOperations._

class ResponseInterceptor private extends MidasInterceptable {

  def read(inputStream: InputStream): Array[Byte] = {
    val header: MongoHeader = MongoHeader(inputStream)
    println(s"RESPONSE $header")
    println(s"RESPONSE MESSAGE LENGTH ${header.length}")
    println(s"RESPONSE PAYLOAD SIZE ${header.payloadSize}")
    println(s"RESPONSE ID ${header.requestID}")
    println(s"RESPONSE ResponseTo ${header.responseTo}")
    println(s"RESPONSE OPCODE ${header.opCode}")
    if(header.hasPayload) modifyPayload(inputStream, header) else header.bytes
  }

  private def modifyPayload(in: InputStream, header: MongoHeader): Array[Byte] = {
    val documents = extractDocumentsFrom(in, header)
    val transformedDocuments = documents map Transformer.transform
    val newPayloadBytes = transformedDocuments flatMap (_.toBytes)
    header.updateLength(newPayloadBytes.length)
    header.bytes ++ newPayloadBytes
    //    import BaseMongoHeader.OpCode._
    //    header.opCode match {
//      case OP_REPLY => println(s"RESPONSE OP_REPLY ***** $documents")
//      case opCode => println(s"RESPONSE $opCode")
//    }
  }
  
  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader): List[BSONObject] = {
    val stream = new FixedSizeStream(inputStream, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map (n => toDocument(stream))
    documents.toList
  }
}

object ResponseInterceptor {
  def apply(): ResponseInterceptor = new ResponseInterceptor()
}
