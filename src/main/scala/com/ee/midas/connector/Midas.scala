package com.ee.midas.connector

import java.net._
import com.ee.midas.data.DuplexPipe
import java.io.IOException
import java.rmi.activation.UnknownObjectException

object Midas extends App {

  val maxClientConnections = 50

  override def main(args:Array[String]):Unit = {

    val (midasHost,midasPort,mongoHost,mongoPort) = (args(0), args(1).toInt, args(2), args(3).toInt)

    val midasSocket = new ServerSocket(midasPort, maxClientConnections, InetAddress.getByName(midasHost))

    while(true) {
      val midasClient = waitForNewConnectionOn(midasSocket)
      println("New connection received...")

      new HandleNewConnection(midasClient, mongoHost, mongoPort).start()
    }
  }

  def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    println("Listnening on port " + serverSocket.getLocalPort() + " for new connections...")
    serverSocket.accept()
  }

  class HandleNewConnection(midasClient: Socket,mongoHost: String, mongoPort: Int) extends Thread {
    override def run = {
        var targetMongo :Socket = null
        try {
        targetMongo = new Socket(mongoHost, mongoPort)
        val connection:DuplexPipe = new DuplexPipe(midasClient.getInputStream(), midasClient.getOutputStream,
                                            targetMongo.getInputStream(), targetMongo.getOutputStream())
        connection.transferData()
        connection.waitForClientToTerminate()
        connection.close
      }
      catch {
        case e:UnknownHostException => println(e.getMessage)
        case e:IOException => println(e.getMessage)
      }
      finally {
        if(midasClient != null)
          midasClient.close()
        if(targetMongo != null)
          targetMongo.close()
      }
    }
  }
}
