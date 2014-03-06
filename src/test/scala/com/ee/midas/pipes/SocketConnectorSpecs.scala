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

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.Socket
import org.specs2.mock.Mockito
import com.ee.midas.pipes.SocketConnector._
import com.ee.midas.interceptor.MidasInterceptable

@RunWith(classOf[JUnitRunner])
class SocketConnectorSpecs extends Specification with Mockito {

   "Socket Connector" should {
       "Create simplex pipe from client to server " in {
         //given
         val client : Socket = mock[Socket]
         val server : Socket = mock[Socket]

         //when
         val pipe = client ==> server

         //then
         pipe.isInstanceOf[SimplexPipe]
       }

      "Create simplex pipe from server to client " in {
        //given
        val client : Socket = mock[Socket]
        val server : Socket = mock[Socket]

        //when
        val pipe = client <== server

        //then
        pipe.isInstanceOf[SimplexPipe]
      }

      "Create duplex pipe between client and server " in {
        //given
        val client : Socket = mock[Socket]
        val server : Socket = mock[Socket]

        //when
        val pipe = client <====> server

        //then
        pipe.isInstanceOf[DuplexPipe]
      }

      "Create request intercepted duplex pipe between client and server " in {
        //given
        val client : Socket = mock[Socket]
        val server : Socket = mock[Socket]
        val interceptable: MidasInterceptable = mock[MidasInterceptable]

        //when
        val pipe = client <===|> (server, interceptable)

        //then
        pipe.isInstanceOf[DuplexPipe]
      }

      "Create response intercepted duplex pipe between client and server " in {
        //given
        val client : Socket = mock[Socket]
        val server : Socket = mock[Socket]
        val interceptable: MidasInterceptable = mock[MidasInterceptable]

        //when
        val pipe = client <|===> (server, interceptable)

        //then
        pipe.isInstanceOf[DuplexPipe]
      }

      "Create intercepted duplex pipe between client and server " in {
        //given
        val client : Socket = mock[Socket]
        val server : Socket = mock[Socket]
        val interceptable: MidasInterceptable = mock[MidasInterceptable]

        //when
        val pipe = client <|==|> (server, interceptable, interceptable)

        //then
        pipe.isInstanceOf[DuplexPipe]
      }

  }
}
