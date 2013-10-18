package com.ee.midas.connector

import java.io.InputStream
import java.lang.Exception
import java.net.Socket
import scala.util.control.Exception

/**
 * Created with IntelliJ IDEA.
 * User: komal
 * Date: 18/10/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
class MongoConnector(host:String, port:Integer) {

  def apply = this

  def connect(): Socket = {
     new Socket(host, port)
  }
}
