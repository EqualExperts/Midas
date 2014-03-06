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

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import java.io.{ByteArrayInputStream, OutputStream, InputStream}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import java.net.InetAddress
import com.ee.midas.config.{ChangeSet, Application}
import org.bson.{BSONObject, BasicBSONObject}
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.transform.Transformer
import com.ee.midas.utils.SynchronizedHolder

@RunWith(classOf[JUnitRunner])
class RequestInterceptorSpecs extends Specification with Mockito {

  trait setup extends Scope {
    val requestID = 1
    val payloadSize = 10
    val bytes =  new Array[Byte](20)
    val collectionName = "randomCollection"

    val transformer: Transformer = mock[Transformer]
    val tracker = mock[MessageTracker]
    val mockSrc = mock[InputStream]
    val header = mock[BaseMongoHeader]
    val changeSet = ChangeSet(2)
    header.hasPayload returns true
    header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
    header.requestID returns requestID
    header.payloadSize returns payloadSize
    header.bytes returns bytes
  }

  sequential
  "Request Interceptor" should {

    "read request from source" in new setup {
      //given
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.read(mockSrc, header)

      //then
      there was one(mockSrc).read(any[Array[Byte]])
    }

    "Do not track for OpCodes other than OP_QUERY and OP_GET_MORE" in new setup {
      //given
      val collectionBytes = collectionName.getBytes
      header.opCode returns BaseMongoHeader.OpCode.OP_DELETE
      val src = new ByteArrayInputStream(collectionBytes)
      header.payloadSize returns collectionBytes.size
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.read(src, header)

      //then
      there was no(tracker).track(requestID, collectionName)
    }

    "intercept OP_GET_MORE request and track requestId and collectionName" in new setup {
      //given
      val collectionBytes = collectionName.getBytes
      val src = new ByteArrayInputStream(collectionBytes)
      header.payloadSize returns collectionBytes.size
      header.opCode returns BaseMongoHeader.OpCode.OP_GET_MORE
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.read(src, header)

      //then
      there was one(tracker).track(requestID, collectionName)
    }

    "intercept OP_QUERY request and track requestId and collectionName" in new setup {
      //given
      val collectionBytes = collectionName.getBytes
      val src = new ByteArrayInputStream(collectionBytes)
      header.payloadSize returns collectionBytes.size
      header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.read(src, header)

      //then
      there was one(tracker).track(requestID, collectionName)
    }

    "write request to target" in new setup {
      //given
      val tgt = mock[OutputStream]
      val data = "request data".getBytes()

      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.write(data, tgt)

      //then
      there was one(tgt).write(data, 0, data.length)
    }

    "transform request for OP_UPDATE " in new setup {
      //given
      val updatePayload: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
       0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
       0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
       0x17.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte,
       0x75.toByte, 0x6d.toByte, 0x65.toByte, 0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
       0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte, 0x15.toByte,
       0x00.toByte, 0x00.toByte, 0x00.toByte, 0x02.toByte, 0x6e.toByte, 0x61.toByte, 0x6d.toByte, 0x65.toByte,
       0x00.toByte, 0x06.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x6d.toByte, 0x69.toByte, 0x64.toByte,
       0x61.toByte, 0x73.toByte, 0x00.toByte, 0x00.toByte)
      val updateRequest = Update(updatePayload)
      val src = new ByteArrayInputStream(updatePayload)
      header.payloadSize returns updatePayload.size
      header.opCode returns BaseMongoHeader.OpCode.OP_UPDATE
      override val collectionName = "mydb.myCollection"
      val document: BasicBSONObject = new BasicBSONObject("name", "midas")
      transformer.transformRequest(document, changeSet.number, collectionName) returns document
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.read(src, header)

      //then
      there was one(transformer).transformRequest(document, changeSet.number, collectionName)
    }

    "transform request for OP_INSERT " in new setup {
      //given
      val insertPayload: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
        0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
        0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x28.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
        0x07.toByte, 0x5f.toByte, 0x69.toByte, 0x64.toByte, 0x00.toByte, 0x52.toByte, 0xf8.toByte, 0x6f.toByte,
        0x75.toByte, 0xe0.toByte, 0x07.toByte, 0xb7.toByte, 0x42.toByte, 0xf7.toByte, 0x3b.toByte, 0x8b.toByte,
        0x7f.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte, 0x75.toByte, 0x6d.toByte, 0x65.toByte,
        0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x60.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte)

      val src = new ByteArrayInputStream(insertPayload)
      header.payloadSize returns insertPayload.size
      header.opCode returns BaseMongoHeader.OpCode.OP_INSERT
      override val collectionName = "mydb.myCollection"
      val insertRequest = Insert(insertPayload)
      val document = insertRequest.extractDocument
      transformer.transformRequest(document, changeSet.number, collectionName) returns document
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(changeSet))

      //when
      reqInterceptor.read(src, header)

      //then
      there was one(transformer).transformRequest(document, changeSet.number, collectionName)
    }

    "Read header" in {
      //given
      val transformer: Transformer = null
      val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
       0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
       0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
      val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
      val tracker = mock[MessageTracker]
      val reqInterceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), SynchronizedHolder(ChangeSet()))

      //when
      val header = reqInterceptor.readHeader(inputStream)

      //then
      header.isInstanceOf[BaseMongoHeader]
    }

    "uses updated changeSet" in new setup {
      //Given
      val insertPayload: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
        0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
        0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x28.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
        0x07.toByte, 0x5f.toByte, 0x69.toByte, 0x64.toByte, 0x00.toByte, 0x52.toByte, 0xf8.toByte, 0x6f.toByte,
        0x75.toByte, 0xe0.toByte, 0x07.toByte, 0xb7.toByte, 0x42.toByte, 0xf7.toByte, 0x3b.toByte, 0x8b.toByte,
        0x7f.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte, 0x75.toByte, 0x6d.toByte, 0x65.toByte,
        0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x60.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte)

      val src = new ByteArrayInputStream(insertPayload)
      header.payloadSize returns insertPayload.size
      header.opCode returns BaseMongoHeader.OpCode.OP_INSERT
      override val collectionName = "mydb.myCollection"
      val insertRequest = Insert(insertPayload)
      val document = insertRequest.extractDocument

      //And Given
      val newChangeSet = ChangeSet(3)
      transformer.transformRequest(document, newChangeSet.number, collectionName) returns document

      //And Given an Interceptor with
      val oldChangeSet = ChangeSet(2)
      val changeSetHolder = SynchronizedHolder(oldChangeSet)
      val interceptor = new RequestInterceptor(tracker, SynchronizedHolder(transformer), changeSetHolder)

      //When
      changeSetHolder(newChangeSet)
      interceptor.read(src, header)

      //Then
      there was one(transformer).transformRequest(document, newChangeSet.number, collectionName)
      there was no(transformer).transformRequest(document, oldChangeSet.number, collectionName)
    }
  }
}
