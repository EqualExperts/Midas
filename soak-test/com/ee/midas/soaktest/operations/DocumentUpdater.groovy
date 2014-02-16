package com.ee.midas.soaktest.operations

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.Mongo
import groovy.transform.Field
import groovy.transform.TupleConstructor

class DocumentUpdater {

    def host
    def port
    def frequency
    def mongo

    def DocumentUpdater(host, port, frequency) {
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
                sleepFor(frequency.every);
                documentsUpdated = 0
            }
        }
    }

    def sleepFor(millis) {
        def time, unit
        (time, unit) = givenTime
        println("update -- sleeping for $time $unit at ${new Date().seconds}")
        unit.sleep(time)
        println("update -- woke up after $time $unit at ${new Date().seconds}")
    }

    def displayDocument(DBObject dbObject) {
        println(dbObject)
    }

}