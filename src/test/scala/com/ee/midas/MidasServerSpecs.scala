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