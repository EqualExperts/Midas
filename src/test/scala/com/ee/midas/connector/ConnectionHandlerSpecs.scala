package com.ee.midas.connector

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.mongodb.MongoClient
import java.net.Socket

@RunWith(classOf[JUnitRunner])
object ConnectionHandlerSpecs extends Specification{
    "connection handler" should {

      "connect with client and read data from mongo" in {
        val connectionHandler: ConnectionHandler = new ConnectionHandler("localhost", 27020, "localhost", 27017)
        connectionHandler.start()
        val application:MongoClient = new MongoClient("localhost", 27020)
        val databases = application.getDatabaseNames()
        application.close()
        databases must_!=null
      }
    }
}
