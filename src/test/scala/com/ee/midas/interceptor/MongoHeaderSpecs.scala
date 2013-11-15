package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.{ByteArrayInputStream, InputStream}
import org.specs2.mock.Mockito

@RunWith(classOf[JUnitRunner])
class MongoHeaderSpecs extends Specification with Mockito {

  "Mongo Header" should {
     "Create a Mongo Header" in {
        val inputStream: InputStream = mock[InputStream]
        val mongoHeader = MongoHeader(inputStream)
        mongoHeader.isInstanceOf[MongoHeader]
     }

    "Check for payload" in {
      val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
        0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte,
        0x00.toByte, 0x00.toByte, 0x00.toByte)
      val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
      val mongoHeader = MongoHeader(inputStream)

      mongoHeader.hasPayload mustEqual true
    }

    "Return payload size" in {
      val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
        0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte,
        0x00.toByte, 0x00.toByte, 0x00.toByte)
      val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
      val mongoHeader = MongoHeader(inputStream)
      val payloadLength = 33

      mongoHeader.payloadSize mustEqual payloadLength
    }

    "Updates Length" in {
      val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
        0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
        0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte,
        0x00.toByte, 0x00.toByte, 0x00.toByte)
      val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
      val mongoHeader = MongoHeader(inputStream)
      val newLength = 50

      mongoHeader.updateLength(newLength)

      mongoHeader.payloadSize mustEqual newLength
    }
  }

}
