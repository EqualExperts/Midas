package com.ee.midas.config

import java.net.ServerSocket
import scala.util.{Failure, Success, Try}

class ServerSetup {
  var midasServerPort = 0
  var mongoServerPort = 0
  var midasServer: ServerSocket = null
  var mongoServer: ServerSocket = null

  private def run(serverName: String, server: ServerSocket) = {
    new Thread(new Runnable {
      def run() = {
        while(!server.isClosed) {
          println(s"$serverName open on: ${server.getLocalPort}")
          Try {
            server.accept()
          } match {
            case Success(socket) => println("Connection Accepted")
            case Failure(t) => println(s"${t.getMessage}")
          }
        }
      }
    }, serverName).start()
  }

  def start = {
    println("BEFORE CLASS INVOKED. STARTING SERVERS")
    midasServer = new ServerSocket(0)
    midasServerPort = midasServer.getLocalPort
    mongoServer = new ServerSocket(0)
    mongoServerPort = mongoServer.getLocalPort
    run("Midas Server",midasServer)
    run("Mongo Server", mongoServer)
  }

  def stop = {
    println("AFTER CLASS INVOKED. SHUTTING DOWN SERVERS")
    midasServer.close()
    mongoServer.close()
  }
}
