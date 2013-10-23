package com.ee.midas.connector

import java.net.{InetAddress, ServerSocket, Socket}
import com.ee.midas.data.DuplexChannel

class ConnectionDelegator(host: String, port: Int, mongoHost: String, mongoPort: Int) extends Thread {

  def mongoConnector = new MongoConnector(host, mongoPort)
  var midasClient: Socket = null
  var midasSocket:ServerSocket = null
  val maxClientConnections = 50

  override def run() = {
    midasSocket = new ServerSocket(port, maxClientConnections, InetAddress.getByName(host))
    while(true) {
      midasClient = midasSocket.accept()
      handleNewConnection()
    }
  }

  def handleNewConnection() = {
    new DuplexChannel(midasClient, mongoConnector.connect()).start()
  }

}
