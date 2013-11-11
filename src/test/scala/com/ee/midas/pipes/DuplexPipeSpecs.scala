package com.ee.midas.pipes

import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.mockito.runners.MockitoJUnitRunner
import org.junit.Test
import org.specs2.matcher.JUnitMustMatchers

/**
 * IMPORTANT NOTE:
 * ===============
 *
 * Specs are written in JUnit style because
 * 1) Conventional style causes the test cases to run multiple times, as described in the link below.
 * https://groups.google.com/forum/#!topic/play-framework/4Fz5TsOKPio
 * 2) This causes numerous problems while testing systems with multi-threaded environment.
 *
 * Also, @RunWith uses MockitoJUnitRunner because the conventional JUnitRunner provided by Specs2
 * is not compatible with JUnit style test cases written here.
 */
@RunWith(classOf[MockitoJUnitRunner])
class DuplexPipeSpecs extends JUnitMustMatchers with Mockito {

      @Test
      def sendAndReceiveData() {
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

        //when
        duplexPipe.start

        //then
        targetMongoOutputStream.toByteArray must beEqualTo(request)
        midasClientOutputStream.toByteArray must beEqualTo(response)
      }

      @Test
      def closeTheRequestAndResponsePipesOnForceStop {
        //given
        val request = mock[SimplexPipe]
        val response = mock[SimplexPipe]

        //when
        val duplexPipe = DuplexPipe(request, response)
        duplexPipe.forceStop

        //then
        there was one(request).forceStop
        there was one(response).forceStop
      }

      @Test
      def closeGracefully {
        //given
        val request = mock[SimplexPipe]
        val response = mock[SimplexPipe]

        //when
        val duplexPipe = DuplexPipe(request, response)
        duplexPipe.stop

        //then
        there was one(request).stop
        there was one(response).stop
      }

 }
