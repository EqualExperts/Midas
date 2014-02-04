package com.ee.midas.soaktest.operations

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.Mongo
import groovy.transform.Field


@Field
def host = "localhost", port = 27020

@Field
def mongo = new Mongo(host, port)

def viewAndUpdateData(String dbName, String collectionName, String fieldToUpdate,
                      Closure updateFunction) {
    DB db = mongo.getDB(dbName)
    DBCollection collection = db.getCollection(collectionName)
    def documents = collection.find()
    while(documents.hasNext()) {
        def document = documents.next()
        displayDocument(document)
        updateFunction(document, fieldToUpdate)
        collection.save(document)
    }
}

def displayDocument(DBObject dbObject) {
    println(dbObject)
}