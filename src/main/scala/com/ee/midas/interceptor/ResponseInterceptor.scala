package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.BSONObject
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.transform.{Transformer, ResponseTransformer}
import com.ee.midas.utils.SynchronizedHolder

//todo: Design changes for later
// Request really needs to be composed of MongoHeader and Transformer
// Current scenario is like anemic domain model where we have header and transformer
// both outside. RequestInterceptor, the client, co-ordinates header, sucks out info from
// request, transforms it, and puts it back in the response.
class ResponseInterceptor (tracker: MessageTracker, transformerHolder: SynchronizedHolder[Transformer])
  extends MidasInterceptable {

  def readHeader(response: InputStream): BaseMongoHeader = {
    val header = MongoHeader(response)
    logInfo(header.toString)
    header
  }

  def read(response: InputStream, header: BaseMongoHeader): Array[Byte] = {
    if (header.hasPayload) {
      modifyIfRequired(response, header.asInstanceOf[MongoHeader])
    }
    else header.bytes
  }
  
  private def modify(response: InputStream, fullCollectionName: String, header: MongoHeader): Array[Byte] = {
    val documents = extractDocumentsFrom(response, header)
    val transformer = transformerHolder.get
    val transformedDocuments = documents map (document => transformer.transformResponse(document, fullCollectionName))
    val newPayloadBytes = transformedDocuments flatMap (_.toBytes)
    header.updateLength(newPayloadBytes.length)
    newPayloadBytes.toArray
  }

  //Info: Why this method?
  //It turns out that passing payload without modifying was failing when returning large number (around 1000) documents
  //from a query.  Hence, in order that client does not bomb, we extract the payload and re-assemble it.
  private def reassemble(response: InputStream, header: MongoHeader): Array[Byte] = {
    val stream = new FixedSizeStream(response, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map { n =>
      val document: BSONObject = stream
      document
    }
    val payloadBytes = documents.toList flatMap (_.toBytes)
    payloadBytes.toArray
  }

  private def modifyIfRequired(response: InputStream, header: MongoHeader): Array[Byte] = {
    val headerBytes = header.bytes
    val requestId = header.responseTo 
    val payloadBytes = (tracker.fullCollectionName(requestId)) match {
      case Some(fullCollectionName) =>
           modify(response, fullCollectionName, header)
      case None => reassemble(response, header)
    }
    tracker untrack requestId
    headerBytes ++ payloadBytes
  }
  
  private def extractDocumentsFrom(response: InputStream, header: MongoHeader): List[BSONObject] = {
    val stream = new FixedSizeStream(response, header.payloadSize)
    val totalDocuments = header.documentsCount
    val documents = 1 to totalDocuments map { n =>
      val document: BSONObject = stream
      document
    }
    documents.toList
  }

  override def toString = s"${getClass.getName}($transformerHolder)"
}
