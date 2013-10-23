package com.ee.midas.connector

class Midas() {


  def start(host: String, port: Int, mongoHost: String, mongoPort: Int) = {
    val delegator:ConnectionDelegator = new ConnectionDelegator(host, port, mongoHost, mongoPort)
    delegator.start()
  }

}
