package com.ee.midas.data

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util
import org.specs2.mock.Mockito;

@RunWith(classOf[JUnitRunner])
object DuplexDataPipeSpecs extends Specification with Mockito {

    "duplex data pipe" should {
       "handle request" in {
         //given
         val mockRequestPipe = mock[SimplexPipe]
         val mockResponsePipe = mock[SimplexPipe]
         val duplexPipe:DuplexDataPipe = new DuplexDataPipe(mockRequestPipe,mockResponsePipe)

         //when
         duplexPipe.handleRequest()

         //then
         there was one(mockRequestPipe).handle()
       }

       "handle response" in {
         //given
         val mockRequestPipe = mock[SimplexPipe]
         val mockResponsePipe = mock[SimplexPipe]
         val duplexPipe:DuplexDataPipe = new DuplexDataPipe(mockRequestPipe,mockResponsePipe)

         //when
         duplexPipe.handleResponse()

         //then
         there was one(mockResponsePipe).handle()
       }
    }
}
