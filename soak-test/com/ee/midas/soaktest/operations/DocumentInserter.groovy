package com.ee.midas.soaktest.operations

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.Mongo
import com.mongodb.util.JSON

class DocumentInserter {

    def host
    def port
    def frequency
    def mongo

    def DocumentInserter(host, port, frequency) {
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
            sleepFor(frequency.every)
        }
    }

    def sleepFor(givenTime) {
        def time, unit
        (time, unit) = givenTime
        println("insert -- sleeping for $time $unit at ${new Date().seconds}")
        unit.sleep(time)
        println("insert -- woke up after $time $unit at ${new Date().seconds}")
    }

}