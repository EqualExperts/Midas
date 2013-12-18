package com.ee.midas.pipes

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, OutputStream, InputStream}
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class InterceptableSpecs extends Specification with Interceptable {
    "Interceptable" should {
      "intercept data" in {
        val data = "Hello World".getBytes()
        val source : ByteArrayInputStream = new ByteArrayInputStream(data)
        val destination : ByteArrayOutputStream = new ByteArrayOutputStream()
        val bytesWritten = intercept(source , destination)
        bytesWritten mustEqual data.length
        destination.toByteArray mustEqual data
      }
    }
}
