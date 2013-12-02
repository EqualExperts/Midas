package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import java.io.{ByteArrayInputStream, OutputStream, InputStream}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import org.bson.{BasicBSONEncoder, BSONEncoder}
import com.mongodb.{DefaultDBDecoder, DBDecoder}

@RunWith(classOf[JUnitRunner])
class RequestInterceptorSpecs extends Specification with Mockito {
  trait setup extends Scope {
    val requestID = 1
    val payloadSize = 10
    val bytes =  new Array[Byte](20)
    val collectionName = "randomCollection"

    val tracker = mock[MessageTracker]

    val mockSrc = mock[InputStream]

    val header = mock[BaseMongoHeader]
    header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
    header.requestID returns requestID
    header.payloadSize returns payloadSize
    header.bytes returns bytes

  }

  "Request Interceptor" should {

     "read request from source" in new setup {
       //given
       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.read(mockSrc, header)

       //then
       there was one(mockSrc).read(any[Array[Byte]])
      }

     "ignores opCodes other than OP_QUERY and OP_GET_MORE" in new setup {

       //given
       val collectionBytes = collectionName.getBytes
       header.opCode returns BaseMongoHeader.OpCode.OP_DELETE
       val src = new ByteArrayInputStream(collectionBytes)
       header.payloadSize returns collectionBytes.size
       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
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
       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
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
       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.read(src, header)

       //then
       there was one(tracker).track(requestID, collectionName)
     }

     "write request to target" in new setup {
       //given
       val tgt = mock[OutputStream]
       val data = "request data".getBytes()

       val reqInterceptor = new RequestInterceptor(tracker)

       //when:
       reqInterceptor.write(data, tgt)

       //then
       there was one(tgt).write(data, 0, data.length)
     }

     "Read header" in {
       val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
       val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
       val tracker = mock[MessageTracker]
       val reqInterceptor = new RequestInterceptor(tracker)
       val header = reqInterceptor.readHeader(inputStream)
       header.isInstanceOf[BaseMongoHeader]
     }
   }

}
