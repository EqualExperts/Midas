package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import java.io.{ByteArrayInputStream, InputStream}
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class BaseMongoHeaderSpecs extends Specification {

    "BaseMongoHeader" should {

      "Create a Base Mongo Header" in {
        //given
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        val baseMongoHeader = BaseMongoHeader(inputStream)

        //then
        baseMongoHeader.isInstanceOf[BaseMongoHeader]
      }

      "Throw exception if header length is zero" in {
        //given
        val headerBytes: Array[Byte] = Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        //then
        BaseMongoHeader(inputStream) must throwA[IllegalArgumentException]
      }

      "Throw exception if header length is greater than MAX length" in {
        //given
        val headerBytes: Array[Byte] = Array(0x01.toByte, 0x00.toByte, 0x00.toByte, 0x20.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        //then
        BaseMongoHeader(inputStream) must throwA[IllegalArgumentException]
      }

      "Check for payload" in {
        //given
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        val baseMongoHeader = BaseMongoHeader(inputStream)

        //then
        baseMongoHeader.hasPayload mustEqual true
      }


      "Return payload size" in {
        //given
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        val baseMongoHeader = BaseMongoHeader(inputStream)

        //then
        val expectedPayloadSize = 49
        baseMongoHeader.payloadSize mustEqual expectedPayloadSize
      }

      "Return message length" in {
        //given
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

        //when
        val baseMongoHeader = BaseMongoHeader(inputStream)

        //then
        val expectedMessageLength = 69
        baseMongoHeader.length mustEqual expectedMessageLength
      }

      "Update message length" in {
        //given
        val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
          0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
          0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
        val inputStream: InputStream = new ByteArrayInputStream(headerBytes)
        val newLength = 50
        val baseMongoHeader = BaseMongoHeader(inputStream)

        //when
        baseMongoHeader.updateLength(newLength)

        //then
        baseMongoHeader.payloadSize mustEqual newLength
      }
    }

}
