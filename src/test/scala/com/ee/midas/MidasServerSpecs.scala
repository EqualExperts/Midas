package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import java.net.{Socket, URL}
import com.ee.midas.config.{ServerSetup, Configuration}
import scala.util.Try
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class MidasServerSpecs extends Specification with Mockito {

    val deltasDir = new File("deltas")
    val serverSetUp = new ServerSetup
    serverSetUp.start

  "Midas Server"  should {

    "initialize the configuration" in {
      //given
      val cmdConfig = CmdConfig(baseDeltasDir = deltasDir.toURI, mongoPort = serverSetUp.mongoServerPort, midasPort = 27020)
      val mockConfiguration = mock[Configuration]
      val server = new MidasServer(cmdConfig) {
        override def parse(url: URL, configFileName: String) = Try {
          mockConfiguration
        }
      }

      //when
      MidasTerminator(server, 2, SECONDS).start()
      server.start

      //then
      there was one(mockConfiguration).start
    }

    "accept a new connection if mongo is available" in {
      //given
      val mockConfiguration = mock[Configuration]
      val cmdConfig = CmdConfig(baseDeltasDir = deltasDir.toURI, mongoPort = serverSetUp.mongoServerPort, midasPort = 27020)
      val server = new MidasServer(cmdConfig) {
        override def parse(url: URL, configFileName: String) = Try {
          mockConfiguration
        }
      }
      MidasTerminator(server, 5, SECONDS).start()
      server.start

      //when
      new Socket(cmdConfig.midasHost, cmdConfig.midasPort)

      //then
      there was one(mockConfiguration).processNewConnection(any[Socket], any[Socket])
    }

    "rejects a connection if mongo is not reachable" in {
      true
    }

    "terminate all the active connections when stopped" in {
      true
    }
  }

}


case class MidasTerminator(server: MidasServer, stopAfter: Int, unit: TimeUnit) extends Thread {
  override def run() = {
    unit.sleep(stopAfter)
    server.stop
  }
}