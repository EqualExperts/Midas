package com.ee.midas.data

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, BufferedInputStream, DataInputStream}
import java.util

@RunWith(classOf[JUnitRunner])
object SimplexPipeSpecs extends Specification{

  "simplex pipe" should {
    "transfer data from source to destination" in {
      //given
      val data = "Hello World".getBytes()
      val source = new ByteArrayInputStream(data)
      val destination = new ByteArrayOutputStream()
      val simplexPipe = new SimplexPipe(source, destination)

      //when
      simplexPipe.start()
      Thread.sleep(2000)
      source.close()
      destination.close()
      simplexPipe.join()

      //then
      destination.toByteArray() must beEqualTo(data)
    }

  }
}
