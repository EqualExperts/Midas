package com.ee.midas.soaktest.operations

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.Mongo
import com.mongodb.util.JSON

class DataInserter {

    def host
    def port
    def frequency
    def mongo

    def DataInserter(host, port, frequency) {
        this.host = host
        this.port = port
        this.frequency = frequency
        mongo = new Mongo(this.host, this.port)
    }

    def insertSampleDataFor(dbName, collectionName, documentTemplate, maxNumOfDocuments) {
        DB db = mongo.getDB(dbName)
        DBCollection collection = db.getCollection(collectionName)
        int docsInserted = 0
        while(docsInserted < maxNumOfDocuments) {
            (1..frequency.batchSize).each {
                def document = JSON.parse(documentTemplate())
                collection.insert(document)
                docsInserted++
            }
            sleepFor(frequency.interval)
        }
    }

    def sleepFor(millis) {
        println("insert -- sleeping for ${millis/1000} secs at ${new Date().seconds}")
        Thread.sleep(millis)
        println("insert -- woke up after ${millis/1000} secs at ${new Date().seconds}")
    }

}