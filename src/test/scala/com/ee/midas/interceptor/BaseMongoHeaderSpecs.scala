package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import java.io.{ByteArrayInputStream, InputStream}
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class BaseMongoHeaderSpecs extends Specification {

    "BaseMongoHeader" should {

      "Create a Base Mongo Header" in {
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val baseMongoHeader = BaseMongoHeader(inputStream)
        baseMongoHeader.isInstanceOf[BaseMongoHeader]
      }

      "Check for payload" in {
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val baseMongoHeader = BaseMongoHeader(inputStream)

        baseMongoHeader.hasPayload mustEqual true
      }


      "Return payload size" in {
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val baseMongoHeader = BaseMongoHeader(inputStream)
        val expectedPayloadLength = 49

        baseMongoHeader.payloadSize mustEqual expectedPayloadLength
      }

      "Return message length" in {
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val baseMongoHeader = BaseMongoHeader(inputStream)
        val expectedMessageLength = 69

        baseMongoHeader.length mustEqual expectedMessageLength
      }

      "Update message length" in {
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val baseMongoHeader = BaseMongoHeader(inputStream)
        val newLength = 50

        baseMongoHeader.updateLength(newLength)

        baseMongoHeader.payloadSize mustEqual newLength
      }
    }

}
