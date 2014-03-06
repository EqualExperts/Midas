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

package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import java.net._
import com.ee.midas.config.Configuration
import scala.util.Try
import java.io.{IOException, File}
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class MidasServerSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val mockConfiguration = mock[Configuration]
    val mockServer = mock[ServerSocket]
    val mockClient = mock[Socket]
    val mockMongoSocket = mock[Socket]
    val cmdConfig = CmdConfig(baseDeltasDir = deltasDir.toURI, mongoPort = 27050, midasPort = 27021)
    var errorLog: String = ""
  }

  val deltasDir = new File("deltas")

  "Midas Server"  should {

    "initialize the configuration" in new Setup {
      //given
      val server = new MidasServer(cmdConfig) {
        override def parse(url: URL, configFileName: String) = Try {
          mockConfiguration
        }
      }

      //when
      MidasTerminator(server, 50, MILLISECONDS).start()
      server.start

      //then
      there was one(mockConfiguration).start
    }

    "accept a new connection if mongo is available" in new Setup {
      //given
      val server = new MidasServer(cmdConfig) {
        override def parse(url: URL, configFileName: String) = Try {
          mockConfiguration
        }

        override def createServerSocket: Try[ServerSocket] = Try {
          mockServer
        }

        override def createMongoSocket: Try[Socket] = Try {
          mockMongoSocket
        }
      }

      mockServer.accept() returns mockClient thenThrows new IOException("stop accepting clients.")
      MidasTerminator(server, 50, MILLISECONDS).start()

      //when
      server.start

      //then
      there was one(mockConfiguration).processNewConnection(mockClient, mockMongoSocket)
    }

    "rejects a connection if mongo is not reachable" in new Setup {
      //given
      val server = new MidasServer(cmdConfig) {
        override def parse(url: URL, configFileName: String) = Try {
          mockConfiguration
        }

        override def logError(msg: String) = {
          errorLog = msg
        }

        override def createServerSocket: Try[ServerSocket] = Try {
          mockServer
        }
      }

      mockServer.accept() returns mockClient thenThrows new IOException("stop accepting clients.")
      MidasTerminator(server, 50, MILLISECONDS).start()

      //when
      server.start

      //then
      errorLog must contain (s"MongoDB on ${cmdConfig.mongoHost}:${cmdConfig.mongoPort} is not available!")
      there was one(mockClient).close()
    }

    "terminate all the active connections when stopped" in new Setup {
      //given
      val server = new MidasServer(cmdConfig) {
        override def parse(url: URL, configFileName: String) = Try {
          mockConfiguration
        }

        override def createServerSocket: Try[ServerSocket] = Try {
          mockServer
        }
      }

      mockServer.accept() returns mockClient thenReturn mock[Socket]

      //when
      MidasTerminator(server, 50, MILLISECONDS).start()
      server.start

      //then
      there was one(mockConfiguration).stop
      there was one(mockServer).close()
    }
  }

}

case class MidasTerminator(server: MidasServer, stopAfter: Int, unit: TimeUnit) extends Thread {
  override def run() = {
    unit.sleep(stopAfter)
    server.stop
  }
}