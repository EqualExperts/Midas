package com.ee.midas.connector

import java.net.Socket

class MongoConnector(host:String, port:Integer) {

  def apply = this

  def connect(): Socket = {
     new Socket(host, port)
  }
}
