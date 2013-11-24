package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.BSONObject
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.utils.Loggable

class ResponseInterceptor (tracker: MessageTracker, transformer: Transformer)
  extends MidasInterceptable with Loggable {

  def logHeader(header: MongoHeader) = {
    log.info(s"RESPONSE $header")
    log.info(s"RESPONSE MESSAGE LENGTH ${header.length}")
    log.info(s"RESPONSE PAYLOAD SIZE ${header.payloadSize}")
    log.info(s"RESPONSE ID ${header.requestID}")
    log.info(s"RESPONSE ResponseTo ${header.responseTo}")
    log.info(s"RESPONSE OPCODE ${header.opCode}")
  }

  def readHeader(inputStream: InputStream): BaseMongoHeader = {
    val header = MongoHeader(inputStream)
    logHeader(header)
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
    val remaining = new Array[Byte](header.payloadSize)
    in.read(remaining)
    remaining
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
    val stream = new FixedSizeStream(inputStream, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map { n =>
      val document: BSONObject = stream
      document
    }
    documents.toList
  }
}
