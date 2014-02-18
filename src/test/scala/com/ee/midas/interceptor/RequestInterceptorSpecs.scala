package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import java.io.{ByteArrayInputStream, OutputStream, InputStream}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import java.net.InetAddress
import com.ee.midas.config.Application
import org.bson.{BSONObject, BasicBSONObject}
import com.ee.midas.transform.DocumentOperations._

@RunWith(classOf[JUnitRunner])
class RequestInterceptorSpecs extends Specification with Mockito {

  trait setup extends Scope {
    val requestID = 1
    val payloadSize = 10
    val bytes =  new Array[Byte](20)
    val collectionName = "randomCollection"

    //todo: convert this to mock and add specs for transformRequest and transformResponse
    val application: Application = mock[Application]
    val ip: InetAddress = null
    val tracker = mock[MessageTracker]

    val mockSrc = mock[InputStream]

    val header = mock[BaseMongoHeader]
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
       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

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
       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

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
       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

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
       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

       //when
       reqInterceptor.read(src, header)

       //then
       there was one(tracker).track(requestID, collectionName)
     }

     "write request to target" in new setup {
       //given
       val tgt = mock[OutputStream]
       val data = "request data".getBytes()

       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

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
       application.transformRequest(document, collectionName, ip) returns document
       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

       //when
       reqInterceptor.read(src, header)

       //then
       there was one(application).transformRequest(document, collectionName, ip)

     }

     "Read header" in {
       //given
       val application: Application = null
       val ip: InetAddress = null
       val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
       val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
       val tracker = mock[MessageTracker]
       val reqInterceptor = new RequestInterceptor(tracker, application, ip)

       //when
       val header = reqInterceptor.readHeader(inputStream)

       //then
       header.isInstanceOf[BaseMongoHeader]
     }
   }

}
