package com.ee.midas.data

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io._
import org.specs2.mock.Mockito

@RunWith(classOf[JUnitRunner])
object SimplexPipeSpecs extends Specification with Mockito {

  "simplex pipe" should {
    "transfer data from source to destination" in {
      //given
      val data = "Hello World".getBytes()
      val source = new ByteArrayInputStream(data)
      val destination = new ByteArrayOutputStream()
      val simplexPipe = new SimplexPipe("test-pipe",source, destination)

      //when
      simplexPipe.run()
      Thread.sleep(2000)
      source.close()
      destination.close()

      //then
      destination.toByteArray() must beEqualTo(data)
    }

    "close on force stop" in {
      //given
      val mockInputStream = mock[InputStream]
      val mockOutputStream = mock[OutputStream]
      val pipe = new SimplexPipe("test-pipe", mockInputStream, mockOutputStream)

      //when
      pipe.forceStop

      //then
      there was one(mockInputStream).close()
      there was one(mockOutputStream).close()
    }

    "stop gracefully" in {
      //given
      val mockInputStream = mock[InputStream]
      val mockOutputStream = mock[OutputStream]
      val pipe = new SimplexPipe("test-pipe", mockInputStream, mockOutputStream)
      val pipeThread = new Thread(pipe)

      //when
      pipeThread.start()
      //then
      pipe.isActive must beTrue

      //when
      pipe.stop
      Thread.sleep(1000)
      //then
      pipe.isActive must beFalse
    }

  }
}
