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

import java.io.{InputStream}
import com.ee.midas.transform.{Transformer}
import com.ee.midas.config.ChangeSet
import com.ee.midas.utils.SynchronizedHolder
import scala.language.postfixOps

class RequestInterceptor (tracker: MessageTracker, transformerHolder: SynchronizedHolder[Transformer], changeSetHolder: SynchronizedHolder[ChangeSet])
  extends MidasInterceptable {
  private val CSTRING_TERMINATION_DELIM = 0

  private def extractFullCollectionName(bytes: Array[Byte]): String = {
    val result : Array[Byte] = bytes.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
 }

  def readHeader(request: InputStream): BaseMongoHeader = {
    val header = BaseMongoHeader(request)
    logInfo(header.toString)
    header
  }

  def read(request: InputStream, header: BaseMongoHeader): Array[Byte] = {
    if (header.hasPayload) {
      val requestWithoutHeader = new Array[Byte](header.payloadSize)
      request.read(requestWithoutHeader)
      modifyIfRequired(requestWithoutHeader, header)
    }
    else
      header.bytes
  }

  import BaseMongoHeader.OpCode._
  private def modifyIfRequired(request: Array[Byte], header: BaseMongoHeader): Array[Byte] = {
    val fullCollectionName = extractFullCollectionName(request)
    header.opCode match {
      case OP_INSERT => return modify(Insert(request), fullCollectionName, header)
      case OP_UPDATE => return modify(Update(request), fullCollectionName, header)
      case OP_QUERY | OP_GET_MORE => tracker.track(header.requestID, fullCollectionName)
      case _ =>
    }
    header.bytes ++ request
  }

  private def modify(request: Request, fullCollectionName: String, header: BaseMongoHeader): Array[Byte] = {
    val document = request.extractDocument
    val transformer = transformerHolder.get
    val changeSet = changeSetHolder.get.number
    val modifiedDocument = transformer.transformRequest(document, changeSet, fullCollectionName)
    val modifiedPayload = request.reassemble(modifiedDocument)
    val newLength = modifiedPayload.length
    header.updateLength(newLength)
    header.bytes ++ modifiedPayload
  }

  override def toString = s"${getClass.getName}($transformerHolder)"
}