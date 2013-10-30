package com.ee.midas.pipes

import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import java.io.{OutputStream, InputStream, ByteArrayOutputStream, ByteArrayInputStream}
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.Test

@RunWith(classOf[MockitoJUnitRunner])
class SimplexPipeSpecs extends JUnitMustMatchers with Mockito {

  val pipeName = "test-pipe"

   @Test
   def transferDataFromSourceToDestination() {
      //given
      val data = "Hello World".getBytes()
      val source = new ByteArrayInputStream(data)
      val destination = new ByteArrayOutputStream()
      val simplexPipe = new SimplexPipe(pipeName,source, destination)

      //when
      simplexPipe.run()

      source.close()
      destination.close()

      //then
      destination.toByteArray() must beEqualTo(data)
    }

   @Test
   def closeOnForceStop() {
      //given
      val mockInputStream = mock[InputStream]
      val mockOutputStream = mock[OutputStream]
      val pipe = new SimplexPipe(pipeName, mockInputStream, mockOutputStream)

      //when
      pipe.forceStop

      //then
      there was one(mockInputStream).close()
      there was one(mockOutputStream).close()
    }

   @Test
   def stopGracefully() {
      //given
      val mockInputStream = mock[InputStream]
      val mockOutputStream = mock[OutputStream]
      val pipe = new SimplexPipe(pipeName, mockInputStream, mockOutputStream)
      scheduleStopToRunAfter(pipe, 550)

      //when
      pipe.run

      //then
      pipe.isActive must beFalse
    }

  def scheduleStopToRunAfter(pipe: SimplexPipe, time: Int):Unit = {
    new Thread  {
      Thread.sleep(time)
      pipe.stop
    }
  }
}
