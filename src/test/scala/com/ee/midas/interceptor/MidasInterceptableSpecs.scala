package com.ee.midas.interceptor

import java.io.{OutputStream, ByteArrayInputStream, InputStream}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MidasInterceptableSpecs extends Specification with MidasInterceptable with Mockito {

  var readHeaderWasInvoked = false
  var sourceReadWasInvoked = false
  "MidasInterceptable" should {
    "Read header" in {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      intercept(src, tgt)

      //then
      readHeaderWasInvoked
    }

    "Read from inputstream" in {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      intercept(src, tgt)

      //then
      sourceReadWasInvoked
    }

    "Write to outputStream" in {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      intercept(src, tgt)

      //then
      there was one(tgt).write(any[Array[Byte]], anyInt, anyInt)
    }
  }

  def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
    sourceReadWasInvoked = true
    Array[Byte]()
  }


  def readHeader(src: InputStream): BaseMongoHeader = {
    readHeaderWasInvoked = true
    mock[BaseMongoHeader]
  }
}
