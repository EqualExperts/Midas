package com.ee.midas


import com.ee.midas.pipes.{SocketConnector, DuplexPipe}
import java.net.{Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.{Accumulator, Loggable}
import com.ee.midas.interceptor.{MessageTracker, RequestInterceptor, ResponseInterceptor}


object Main extends App with Loggable {

  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {

    val (midasHost,midasPort,mongoHost,mongoPort) = (args(0), args(1).toInt, args(2), args(3).toInt)
    val midasSocket = new ServerSocket(midasPort, maxClientConnections, InetAddress.getByName(midasHost))
    val accumulate = Accumulator[DuplexPipe](Nil)

    sys.ShutdownHookThread {
      val pipes = accumulate(null)
      log.info("User Forced Stop on Midas...Closing Open Connections = ")
      pipes filter(_.isActive) map(_.forceStop)
    }

    import SocketConnector._
    while (true) {
      val application = waitForNewConnectionOn(midasSocket)
      log.info("New connection received...")
      //TODO: do something if Mongo is not available

      val mongoSocket = new Socket(mongoHost, mongoPort)
      val tracker = new MessageTracker()
      val requestInterceptable = new RequestInterceptor(tracker)
      val responseInterceptable = new ResponseInterceptor(tracker)

      //      val duplexPipe = application  <|===> (mongoSocket, ResponseInterceptor())
      val duplexPipe = application  <|==|> (mongoSocket, requestInterceptable, responseInterceptable)

      duplexPipe.start
      log.info("Setup DataPipe = " + duplexPipe.toString)
      accumulate(duplexPipe)
    }
  }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    log.info("Listening on port " + serverSocket.getLocalPort() + " for new connections...")
    serverSocket.accept()
  }
}
