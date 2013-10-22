package com.ee.midas.connector

import java.net.{InetAddress, Socket, ServerSocket}
import com.ee.midas.data.{DuplexDataPipe, SimplexPipe}


class ConnectionHandler(host: String, port: Int, mongoHost:String, mongoPort:Int) extends Thread {

  var midasClient: Socket = null
  var targetMongo: Socket = null
  var midasSocket:ServerSocket = null
  def mongoConnector = new MongoConnector(host,mongoPort)

  private def openConnection() = {
    midasSocket = new ServerSocket(port, 10, InetAddress.getByName(host))
    midasClient = midasSocket.accept()
    targetMongo = mongoConnector.connect()
  }

  override def run() = {
    openConnection()
    handleData()
    closeConnection()
  }

  def handleData() = {
    val requestPipe = new SimplexPipe(midasClient.getInputStream(), targetMongo.getOutputStream())
    val responsePipe = new SimplexPipe(targetMongo.getInputStream(), midasClient.getOutputStream())
    val dataPipe = new DuplexDataPipe(requestPipe, responsePipe)

    while(midasClient.isConnected() && targetMongo.isConnected()){
      dataPipe.handleRequest()
      dataPipe.handleResponse()
    }
  }

  def closeConnection() = {
    midasClient.close()
    targetMongo.close()
  }
}

