package com.ee.midas.connector

import java.net.{Socket, ServerSocket}

class Proxy(port: Int, host: String, mongoPort:Int) extends Thread {

  var client: Socket = null
  var mongo: Socket = null
  var serverSocket:ServerSocket = null
  def mongoConnector = new MongoConnector(host,mongoPort)

  private def openConnection() = {
    serverSocket = new ServerSocket(port)
    client = serverSocket.accept()
    mongo = mongoConnector.connect()
  }

  override def run()  = {
    openConnection()
    val dataPipe = new DataPipe()
    println("client is connected? " + client.isConnected())
    println("server is connected? " + mongo.isConnected())
    while(client.isConnected() && mongo.isConnected()){
      if(client.getInputStream.available() > 0) {
        dataPipe.writeToServer(mongo.getOutputStream(), dataPipe.readFromClient(client.getInputStream()))
      }
      if(mongo.getInputStream.available() > 0) {
        dataPipe.writeToServer(client.getOutputStream(), dataPipe.readFromClient(mongo.getInputStream()))
      }
    }
  }
}
