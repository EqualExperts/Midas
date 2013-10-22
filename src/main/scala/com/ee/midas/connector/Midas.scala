package com.ee.midas.connector

//TODO: Accept IP as well for Midas to be listening on
class Midas()  {

  def start(host: String, port: Int, mongoHost:String, mongoPort:Int) = {
    new ConnectionHandler(host, port, mongoHost, mongoPort).start()
  }

}
