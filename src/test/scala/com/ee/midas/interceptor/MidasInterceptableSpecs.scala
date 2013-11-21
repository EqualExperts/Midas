package com.ee.midas.interceptor

import java.io.{OutputStream, ByteArrayInputStream, InputStream}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class MidasInterceptableSpecs extends Specification with MidasInterceptable with Mockito {

  var readHeaderInvoked = false
  var sourceReadInvoked = false
  "MidasInterceptable" should {
    "Read header" in {
      val src = mock[InputStream]
      val tgt = mock[OutputStream]
      intercept(src, tgt)
      readHeaderInvoked
    }

    "Read from inputstream" in {
      val src = mock[InputStream]
      val tgt = mock[OutputStream]
      intercept(src, tgt)
      sourceReadInvoked
    }

    "Write to outputStream" in {
      val src = mock[InputStream]
      val tgt = mock[OutputStream]
      intercept(src, tgt)
      there was one(tgt).write(any[Array[Byte]], anyInt, anyInt)
    }
  }

  def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
    sourceReadInvoked = true
    Array[Byte]()
  }


  def readHeader(src: InputStream): BaseMongoHeader = {
    readHeaderInvoked = true
    mock[BaseMongoHeader]
  }
}
