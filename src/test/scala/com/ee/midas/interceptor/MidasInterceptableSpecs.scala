package com.ee.midas.interceptor

import java.io.{OutputStream, ByteArrayInputStream, InputStream}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.config.Application
import com.ee.midas.transform.TransformType
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class MidasInterceptableSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val application = Application(null, "SomeApp", TransformType.EXPANSION, Nil)
    val midasInterceptable = new MidasInterceptable(application, null) {
      def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
        sourceReadWasInvoked = true
        Array[Byte]()
      }


      def readHeader(src: InputStream): BaseMongoHeader = {
        readHeaderWasInvoked = true
        mock[BaseMongoHeader]
      }
    }

    var readHeaderWasInvoked = false
    var sourceReadWasInvoked = false
  }


  "MidasInterceptable" should {
    "Read header" in new Setup {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      midasInterceptable.intercept(src, tgt)

      //then
      readHeaderWasInvoked
    }

    "Read from inputstream" in new Setup {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      midasInterceptable.intercept(src, tgt)

      //then
      sourceReadWasInvoked
    }

    "Write to outputStream" in new Setup {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      midasInterceptable.intercept(src, tgt)

      //then
      there was one(tgt).write(any[Array[Byte]], anyInt, anyInt)
    }
  }

}
