package com.ee.midas.pipes

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.Socket
import org.specs2.mock.Mockito
import com.ee.midas.pipes.SocketConnector._


@RunWith(classOf[JUnitRunner])
class SocketConnectorSpecs extends Specification with Mockito {

    "Socket Connector" should {
       "Create simplex pipe from client to server " in {
          val client : Socket = mock[Socket]
          val server : Socket = mock[Socket]
          val pipe = client ===> server
          pipe.isInstanceOf[SimplexPipe]
       }

      "Create simple pipe from server to client " in {
        val client:Socket = mock[Socket]
        val server:Socket = mock[Socket]
        val pipe = client <=== server
        pipe.isInstanceOf[SimplexPipe]
      }

      "Create duplex pipe between client and server " in {
        val client:Socket = mock[Socket]
        val server:Socket = mock[Socket]
        val pipe = client <====> server
        pipe.isInstanceOf[DuplexPipe]
      }
    }
}
