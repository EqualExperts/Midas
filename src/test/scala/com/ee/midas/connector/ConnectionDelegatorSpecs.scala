package com.ee.midas.connector

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.mongodb.MongoClient
import java.net.Socket

@RunWith(classOf[JUnitRunner])
object ConnectionDelegatorSpecs extends Specification{
    "connection delegator" should {

      "accept connection from client and read data from mongo" in {
        val connectionReceiver: ConnectionDelegator = new ConnectionDelegator("localhost", 27020, "localhost", 27017)
        connectionReceiver.start()
        val application:MongoClient = new MongoClient("localhost", 27020)
        val databases = application.getDatabaseNames()
        println(databases)
        application.close()
        databases must_!=null
      }
    }
}
