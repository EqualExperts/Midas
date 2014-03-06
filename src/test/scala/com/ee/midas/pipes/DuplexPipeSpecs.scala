/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.pipes

import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.mockito.runners.MockitoJUnitRunner
import org.junit.Test
import org.specs2.matcher.JUnitMustMatchers

/**
 * IMPORTANT NOTE:
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
        val request: Array[Byte] = "Hello World Request".getBytes()
        val response: Array[Byte] = "Hello World Response".getBytes()
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
        Thread.sleep(500)
        (targetMongoOutputStream.toByteArray).mustEqual(request)
        (midasClientOutputStream.toByteArray).mustEqual(response)
        targetMongoOutputStream.close
        midasClientOutputStream.close
        targetMongoInputStream.close
        midasClientInputStream.close
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
