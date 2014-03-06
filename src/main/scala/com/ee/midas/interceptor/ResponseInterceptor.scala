/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

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
