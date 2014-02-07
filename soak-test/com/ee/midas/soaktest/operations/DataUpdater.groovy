package com.ee.midas.soaktest.operations

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.Mongo
import groovy.transform.Field
import groovy.transform.TupleConstructor

class DataUpdater {

    def host
    def port
    def frequency
    def mongo

    def DataUpdater(host, port, frequency) {
        this.host = host
        this.port = port
        this.frequency = frequency
        mongo = new Mongo(this.host, this.port)
    }

    def viewAndUpdateData(String dbName, String collectionName, String fieldToUpdate,
                          Closure updateFunction) {
        DB db = mongo.getDB(dbName)
        DBCollection collection = db.getCollection(collectionName)
        def documents = collection.find()
        int documentsUpdated = 0
        while(documents.hasNext()) {
            def document = documents.next()
            displayDocument(document)
            updateFunction(document, fieldToUpdate)
            collection.save(document)
            if(++documentsUpdated >= frequency.batchSize){
                sleepFor(frequency.interval);
                documentsUpdated = 0
            }
        }
    }

    def sleepFor(millis) {
        println("update -- sleeping for ${millis/1000} secs at ${new Date().seconds}")
        Thread.sleep(millis)
        println("update -- woke up after ${millis/1000} secs at ${new Date().seconds}")
    }

    def displayDocument(DBObject dbObject) {
        println(dbObject)
    }

}