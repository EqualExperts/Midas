package com.ee.midas.connector

import java.net._
import com.ee.midas.data.{DuplexPipe, SimplexPipe}

object Midas extends App {

  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {

    val (midasHost,midasPort,mongoHost,mongoPort) = (args(0), args(1).toInt, args(2), args(3).toInt)

    val midasSocket = new ServerSocket(midasPort, maxClientConnections, InetAddress.getByName(midasHost))

    sys.ShutdownHookThread {
      println("Midas exiting")
    }

    while(true) {
      val midasClient = waitForNewConnectionOn(midasSocket)
      //TODO: do something if Mongo is not available
      val mongoSocket = new Socket(mongoHost, mongoPort)
      createEndPoints(midasClient, mongoSocket)
      println("New connection received...")
    }


  }


  private def createEndPoints(midasClient: Socket, mongoSocket: Socket) {
    val clientIn = midasClient.getInputStream
    val serverOut = mongoSocket.getOutputStream
    val requestPipe = new SimplexPipe("Request", clientIn, serverOut)

    val serverIn = mongoSocket.getInputStream
    val clientOut = midasClient.getOutputStream
    val responsePipe = new SimplexPipe("Response", serverIn, clientOut)

    val duplexPipe = DuplexPipe(requestPipe, responsePipe)
    duplexPipe.start
  }

  def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    println("Listening on port " + serverSocket.getLocalPort() + " for new connections...")
    serverSocket.accept()
  }
}
