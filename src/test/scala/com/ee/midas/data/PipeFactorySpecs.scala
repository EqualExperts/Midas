package com.ee.midas.data

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.Socket
import org.specs2.mock.Mockito
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}

@RunWith(classOf[JUnitRunner])
object PipeFactorySpecs extends Specification with Mockito{

  "pipe factory" should {  "create simplex connection for a response" in {
        //given
        val mockMidasClient = mock[Socket]
        val mockTargetMongo = mock[Socket]
        val stubInputStream = new ByteArrayInputStream(new Array[Byte](10))
        val stubOutputStream = new ByteArrayOutputStream()

        mockMidasClient.getInputStream() returns (stubInputStream)
        mockTargetMongo.getOutputStream() returns (stubOutputStream)

        val pipeFactory:PipeFactory = new PipeFactory()

        //when
        val pipe:SimplexPipe = pipeFactory.createSimplexPipe(mockMidasClient,mockTargetMongo)

        //then
        pipe.source == stubInputStream
        pipe.destination == stubOutputStream
      }
  }
}
