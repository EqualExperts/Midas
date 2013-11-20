package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.{ByteArrayInputStream, InputStream}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ResponseInterceptorSpecs extends Specification with Mockito {

    "Response Interceptor" should {
      "Read Header" in {
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte,
          0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val tracker = mock[MessageTracker]
        val resInterceptor = new ResponseInterceptor(tracker)
        val header = resInterceptor.readHeader(inputStream)
        header.isInstanceOf[MongoHeader]
      }

      "read request from source" in new setup {
        //given

        val resInterceptor = new ResponseInterceptor(tracker)

        //when:
        resInterceptor.read(mockSrc, header)

        //then
        there was one(mockSrc).read(any[Array[Byte]])
      }

    }

  trait setup extends Scope {
    val requestID = 1
    val payloadSize = 10
    val bytes =  new Array[Byte](20)
    val collectionName = "randomCollection"

    val tracker = mock[MessageTracker]

    val mockSrc = mock[InputStream]

    val header = mock[MongoHeader]
    header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
    header.requestID returns requestID
    header.payloadSize returns payloadSize
    header.bytes returns bytes

  }
}


