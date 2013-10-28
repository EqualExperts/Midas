package com.ee.midas.data

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}

@RunWith(classOf[JUnitRunner])
object DuplexPipeSpecs extends Specification with Mockito {

    "duplex pipe" should {

      "send and receive data" in {
        // given
        val request = "Hello World Request".getBytes()
        val response = "Hello World Response".getBytes()
        val midasClientInputStream = new ByteArrayInputStream(request)
        val midasClientOutputStream = new ByteArrayOutputStream()
        val targetMongoInputStream = new ByteArrayInputStream(response)
        val targetMongoOutputStream = new ByteArrayOutputStream()
        val requestPipe = new SimplexPipe("request", midasClientInputStream, targetMongoOutputStream)
        val responsePipe = new SimplexPipe("response", targetMongoInputStream, midasClientOutputStream)

        val duplexPipe = DuplexPipe(requestPipe, responsePipe)
        Thread.sleep(1000)
        //when
        duplexPipe.start

        //then
        targetMongoOutputStream.toByteArray must beEqualTo(request)
        midasClientOutputStream.toByteArray must beEqualTo(response)
      }

      "close the request and response pipes on forceStop" in {
        //given
        val request = mock[SimplexPipe]
        val response = mock[SimplexPipe]

        //when
        val duplexPipe = DuplexPipe(request, response)
        Thread.sleep(1000)
        duplexPipe.forceStop

        //then
        there was one(request).forceStop
        there was one(response).forceStop
      }

      "close gracefully" in {
        //given
        val request = mock[SimplexPipe]
        val response = mock[SimplexPipe]

        //when
        val duplexPipe = DuplexPipe(request, response)
        Thread.sleep(1000)
        duplexPipe.stop

        //then
        there was one(request).stop
        there was one(response).stop
      }
    }
}
