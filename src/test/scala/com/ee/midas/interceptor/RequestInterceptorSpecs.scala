package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import java.io.{OutputStream, InputStream}

class RequestInterceptorSpecs extends Specification with Mockito {

   "Request Interceptor" should {

     val requestID = anyInt
     val payloadSize = anyInt
     val bytes =  Array(anyByte)
     val collectionName = anyString

     "read request from source" in {
       //given
       val tracker = mock[MessageTracker]

       val src = mock[InputStream]

       val header = mock[BaseMongoHeader]
       header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
       header.requestID returns requestID
       header.payloadSize returns payloadSize
       header.bytes returns bytes

       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.read(src, header)

       //then
       there was one(src).read(any[Array[Byte]])
      }

     "intercept opCode and requestId from header" in {
       //given
       val tracker = mock[MessageTracker]

       val src = mock[InputStream]

       val header = mock[BaseMongoHeader]
       header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
       header.requestID returns requestID
       header.payloadSize returns payloadSize
       header.bytes returns bytes

       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.read(src, header)

       //then
       there was one(header).opCode
       there was one(header).requestID
     }

     "add the requestId and collectionName to tracker" in {
       //given
       val tracker = mock[MessageTracker]

       val src = mock[InputStream]

       val header = mock[BaseMongoHeader]
       header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
       header.requestID returns requestID
       header.payloadSize returns payloadSize
       header.bytes returns bytes

       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.read(src, header)

       //then
       there was one(tracker).track(requestID, collectionName)
     }

     "write request to target" in {
       //given
       val tracker = mock[MessageTracker]

       val tgt = mock[OutputStream]
       val data = "request data".getBytes()

       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.write(data, tgt)

       //then
       there was one(tgt).write(data, 0, data.length)
     }
   }

}
