package com.ee.midas


import com.ee.midas.pipes.{SocketConnector, DuplexPipe, SimplexPipe}
import org.slf4j.LoggerFactory
import java.net.{Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.Loggable


object Main extends App with Loggable {

  def startWith(initial : List[DuplexPipe]) : DuplexPipe => List[DuplexPipe] = {
    var acc = initial
    (pipe: DuplexPipe) => {
      acc = if(pipe == null) acc else pipe :: acc
      acc
    }
  }

  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {

    val (midasHost,midasPort,mongoHost,mongoPort) = (args(0), args(1).toInt, args(2), args(3).toInt)
    val midasSocket = new ServerSocket(midasPort, maxClientConnections, InetAddress.getByName(midasHost))
    val accumulate = startWith(Nil)

    sys.ShutdownHookThread {
      val pipes = accumulate(null)
      log.info("User Forced Stop on Midas...Closing Open Connections = ")
      pipes filter(_.isActive) map(_.forceStop)
    }

    import SocketConnector._
    while (true) {
      val midasClient = waitForNewConnectionOn(midasSocket)
      log.info("New connection received...")
      //TODO: do something if Mongo is not available
      val mongoSocket = new Socket(mongoHost, mongoPort)
      val duplexPipe = midasClient <==> mongoSocket
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
