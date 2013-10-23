package com.ee.midas.data

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import java.net.Socket

@RunWith(classOf[JUnitRunner])
object DuplexChannelSpecs extends Specification with Mockito {

    "duplex channel" should {

      "starts a request and response thread" in {
        // given
        val mockRequest = mock[SimplexPipe]
        val mockResponse = mock[SimplexPipe]
        val mockMidasClient = mock[Socket]
        val mockTargetMongo = mock[Socket]
        val mockPipeFactory = mock[PipeFactory]
        val duplexChannel:DuplexChannel = new DuplexChannel(mockMidasClient,mockTargetMongo)
        duplexChannel.pipeFactory = mockPipeFactory
        mockPipeFactory.createSimplexPipe(mockMidasClient, mockTargetMongo) returns mockRequest
        mockPipeFactory.createSimplexPipe(mockTargetMongo, mockMidasClient) returns mockResponse

        //when
        duplexChannel.handleData()

        //then
        there was one(mockPipeFactory).createSimplexPipe(mockMidasClient,mockTargetMongo)
        there was one(mockPipeFactory).createSimplexPipe(mockTargetMongo,mockMidasClient)
        there was one(mockRequest).start()
        there was one(mockResponse).start()
        there was one(mockRequest).join()
      }

    }
}
