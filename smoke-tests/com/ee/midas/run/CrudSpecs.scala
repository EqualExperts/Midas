package com.ee.midas.run

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.Specification
import com.mongodb._

@RunWith(classOf[JUnitRunner])
class CrudSpecs extends Specification {

    var application: MongoClient = null
    var document:DBObject = null

    def is = s2"""
    Narration:
    //TODO: write a story to represent CRUD.
    This is a specification to verify that midas behaves as a proxy

    A client application should
        Step 1: Ensure Midas and mongods are running
            Connect to Midas                 $connect

        Step 2: Perform CRUD operations
            insert documents                 $insert
            read documents                   $read
            update documents                 $update
            delete documents                 $delete

        Step 3: Close connection to Midas
            Disconnect                       $disconnect
                                                               """

    val connect = {
       application = new MongoClient("localhost", 27020)
       application.getConnector.isOpen
    }

    val insert = {
        document = new BasicDBObject("testName","midas is a proxy")
        val database:DB = application.getDB("midasSmokeTest")
        val collection:DBCollection = database.getCollection("tests")
        val result:WriteResult = collection.insert(document)
        result.getError == null
    }

    val read = {
        val database:DB = application.getDB("midasSmokeTest")
        val collection:DBCollection = database.getCollection("tests")
        val readDocument:DBObject = collection.findOne()
        readDocument == document
    }

    val update = {
        val database:DB = application.getDB("midasSmokeTest")
        val collection:DBCollection = database.getCollection("tests")
        val document = collection.findOne
        document.put("version", 1)
        val result:WriteResult = collection.update(collection.findOne, document)
        result.getError == null
    }

    val delete = {
        val database:DB = application.getDB("midasSmokeTest")
        val collection:DBCollection = database.getCollection("tests")
        val result:WriteResult = collection.remove(document)
        result.getError == null
    }

    val disconnect = {
        application.close()
        application.getConnector.isOpen must beFalse
    }

}
