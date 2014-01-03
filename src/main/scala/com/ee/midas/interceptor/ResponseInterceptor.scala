package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.BSONObject
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.utils.Loggable
import com.ee.midas.transform.Transformer

class ResponseInterceptor (tracker: MessageTracker, transformer: Transformer)
  extends MidasInterceptable with Loggable {

  def readHeader(inputStream: InputStream): BaseMongoHeader = {
    val header = MongoHeader(inputStream)
    log.info(header.toString)
    header
  }

  def read(inputStream: InputStream, header: BaseMongoHeader): Array[Byte] = {
    if (header.hasPayload) {
      modifyPayloadIfRequired(inputStream, header.asInstanceOf[MongoHeader])
    }
    else header.bytes
  }
  
  private def modifyPayload(in: InputStream, header: MongoHeader)(implicit fullCollectionName: String): Array[Byte] = {
    val documents = extractDocumentsFrom(in, header)
    val transformedDocuments = documents map transformer.transform
    val newPayloadBytes = transformedDocuments flatMap (_.toBytes)
    header.updateLength(newPayloadBytes.length)
    newPayloadBytes.toArray
  }
  
  private def payload(in: InputStream, header: MongoHeader): Array[Byte] = {
    val stream = new FixedSizeStream(in, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map { n =>
      val document: BSONObject = stream
      document
    }
    val payloadBytes = documents.toList flatMap (_.toBytes)
    payloadBytes.toArray
  }

  private def modifyPayloadIfRequired(in: InputStream, header: MongoHeader): Array[Byte] = {
    val headerBytes = header.bytes
    val requestId = header.responseTo 
    val payloadBytes = (tracker.fullCollectionName(requestId)) match {
      case Some(fcName) =>
        implicit val fullCollectionName = fcName
        if (transformer.canTransformDocuments) {
           modifyPayload(in, header)
        } else {
           payload(in, header)
        }
      case None => payload(in, header)
    }
    tracker untrack requestId
    headerBytes ++ payloadBytes
  }
  
  private def extractDocumentsFrom(inputStream: InputStream, header: MongoHeader): List[BSONObject] = {
    log.info(s"Payload Size")
    val stream = new FixedSizeStream(inputStream, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map { n =>
      val document: BSONObject = stream
      document
    }
    documents.toList
  }
}
