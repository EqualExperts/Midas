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
       //given
       val headerBytes: Array[Byte] = Array(0x45.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x1d.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x21.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x08.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte)
       val inputStream: InputStream = new ByteArrayInputStream(headerBytes)

       //when
       val mongoHeader = MongoHeader(inputStream)

       //then
       mongoHeader.isInstanceOf[MongoHeader]
     }
  }

}
