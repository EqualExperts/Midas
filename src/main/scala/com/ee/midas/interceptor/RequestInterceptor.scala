package com.ee.midas.interceptor

import com.ee.midas.pipes.Interceptable
import java.io.{OutputStream, InputStream}
import org.bson.{BasicBSONCallback, BasicBSONDecoder, BSONObject}


class RequestInterceptor private extends Interceptable {
  val decoder = new BasicBSONDecoder()
  override def intercept(src: InputStream, tgt: OutputStream): Int = {
    val request = read(src)
    val callback = new BasicBSONCallback()
    decoder.decode(src, callback)
//    write(callback.got
//      , tgt)

//    val request = read(src)
//    write(request, tgt)
  }

  private def read(inputStream: InputStream): Array[Byte] = {
    val header: MongoHeader = MongoHeader(inputStream)
    println(s"REQUEST $header")
    if(header.hasPayload) extractPayload(inputStream, header) else header.bytes
  }

  private def write(data: Array[Byte], tgt: OutputStream): Int = {
    val bytesToWrite = data.length
    tgt.write(data, 0, bytesToWrite)
    tgt.flush()
    bytesToWrite
  }

  import MongoHeader.OpCode._
//  private val decoder = new NewBSONDecoder()

  private def extractPayload(in: InputStream, header: MongoHeader) = {
    header.opCode match {
      case OP_QUERY => println(s"REQUEST OP_QUERY = *****")
      case OP_MSG => println(s"REQUEST OP_MSG ***** ")
      case OP_GET_MORE => println(s"REQUEST OP_REPLY ***** ")
      case opCode => println(s"REQUEST $opCode")
    }
    val documents = extractDocumentsFrom(in, header)
//    header.bytes ++ documents.toBytes
    constructResponseUsing(documents, header)
  }

  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader): List[BSONObject] = {
    import DocumentConverter._
    val stream = new FixedSizeStream(inputStream, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map (n => toDocument(stream))
    documents.toList
  }

  private def constructResponseUsing(documents: List[BSONObject], header: MongoHeader): Array[Byte] = {
    import com.ee.midas.transform.DocumentOperations._
    val payloadBytes = documents flatMap (_.toBytes)
    header.updateLength(payloadBytes.length)
    header.bytes ++ payloadBytes
  }

}

object RequestInterceptor {
  def apply(): RequestInterceptor = new RequestInterceptor()
}

