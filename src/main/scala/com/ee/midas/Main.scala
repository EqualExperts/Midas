package com.ee.midas

import java.net._
import com.ee.midas.pipes.{DuplexPipe, SimplexPipe}

object Main extends App {

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
      println("User Forced Stop on Midas...Closing Open Connections = ")
      pipes filter(_.isActive) map(_.forceStop)
    }

    while (true) {
      val midasClient = waitForNewConnectionOn(midasSocket)
      println("New connection received...")
      //TODO: do something if Mongo is not available
      val mongoSocket = new Socket(mongoHost, mongoPort)
      val pipe = createDuplexPipe(midasClient, mongoSocket)
      println("Setup DataPipe = " + pipe.toString)
      accumulate(pipe)
    }
  }

  private def createDuplexPipe(client: Socket, server: Socket) = {
    val clientIn = client.getInputStream
    val serverOut = server.getOutputStream
    val requestPipe = new SimplexPipe("Request", clientIn, serverOut)

    val serverIn = server.getInputStream
    val clientOut = client.getOutputStream
    val responsePipe = new SimplexPipe("Response", serverIn, clientOut)

    val duplexPipe = DuplexPipe(requestPipe, responsePipe)
    duplexPipe.start
    duplexPipe
  }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    println("Listening on port " + serverSocket.getLocalPort() + " for new connections...")
    serverSocket.accept()
  }
}
