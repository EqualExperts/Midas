package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.{OutputStream, ByteArrayInputStream, InputStream}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.mongodb.BasicDBObject
import org.bson.BasicBSONEncoder
import com.ee.midas.config.Application
import java.net.InetAddress
import com.ee.midas.transform.Transformer
import com.ee.midas.utils.SynchronizedHolder

@RunWith(classOf[JUnitRunner])
class ResponseInterceptorSpecs extends Specification with Mockito {

    sequential
    "Response Interceptor" should {
      "Read Header" in new setup {

        //given
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte,
          0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        val responseHeader = resInterceptor.readHeader(inputStream)

        //then
        responseHeader.isInstanceOf[MongoHeader]
      }

      "intercept and transform payload" in new setup {
        //given
        val transformedPayload = new BasicDBObject("value", 1)
        transformedPayload.put("version", 1)

        header.hasPayload returns true
        header.responseTo returns responseID
        header.documentsCount returns 1
        header.payloadSize returns payloadBytes.length
        tracker.fullCollectionName(responseID) returns Option(collectionName)

        transformer.transformResponse(payloadData, collectionName) returns transformedPayload

        //when:
        val readData = resInterceptor.read(src, header)

        //then
        readData mustEqual (header.bytes ++ encoder.encode(transformedPayload))
        there was one(transformer).transformResponse(payloadData, collectionName)
      }

      "write response to target" in new setup {
        //given
        val tgt = mock[OutputStream]
        val data = "response data".getBytes()


        //when:
        resInterceptor.write(data, tgt)

        //then
        there was one(tgt).write(data, 0, data.length)
      }

      "return header in case of no payload" in new setup {
        //given
        header.hasPayload returns false

        //when:
        val readData = resInterceptor.read(mockSrc, header)

        //then
        readData mustEqual header.bytes
      }

      "return payload as is for untracked collections" in new setup {

        //given
        header.hasPayload returns true
        header.responseTo returns responseID
        header.payloadSize returns payloadBytes.length
        header.documentsCount returns 1
        tracker.fullCollectionName(responseID) returns None

        //when:
        val readData = resInterceptor.read(src, header)

        //then
        readData mustEqual (header.bytes ++ payloadBytes)
      }

    }

  trait setup extends Scope {
    val requestID = 1
    val responseID = 1
    val bytes =  new Array[Byte](20)
    val collectionName = "randomCollection"

    val tracker = mock[MessageTracker]

    val mockSrc = mock[InputStream]

    val header = mock[MongoHeader]
    header.opCode returns BaseMongoHeader.OpCode.OP_QUERY
    header.requestID returns requestID

    header.bytes returns bytes
    val transformer = mock[Transformer]
    val ignoreIp : InetAddress = null

    val resInterceptor = new ResponseInterceptor(tracker, SynchronizedHolder(transformer))
    val payloadData = new BasicDBObject("value", 1)
    val encoder = new BasicBSONEncoder()
    val payloadBytes = encoder.encode(payloadData)

    val src = new ByteArrayInputStream(payloadBytes)
  }
}


