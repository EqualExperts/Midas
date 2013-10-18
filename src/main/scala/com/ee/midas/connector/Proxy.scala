package com.ee.midas.connector

import java.net.{Socket, ServerSocket}

/**
 * Created with IntelliJ IDEA.
 * User: komal
 * Date: 18/10/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
class Proxy(port: Int, host: String, mongoPort:Int) extends Thread {

  var client: Socket = null
  var mongo: Socket = null
  def mongoConnector = new MongoConnector(host,mongoPort)

  private def openConnection() = {
    val serverSocket:ServerSocket = new ServerSocket(port)
    client = serverSocket.accept()
    mongo = mongoConnector.connect()
  }

  override def run()  = {
    openConnection()
  }
}
