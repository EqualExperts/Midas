package com.ee.midas.soaktest.operations

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.Mongo
import groovy.transform.Field

@Field
def host = "localhost", port = 27020

@Field
def mongo = new Mongo(host, port)

def insertSampleDataFor(dbName, collectionName, fields, maxNumOfDocuments) {
    DB db = mongo.getDB(dbName)
    DBCollection collection = db.getCollection(collectionName)
    (1..maxNumOfDocuments).each { fieldValue ->
        def document = new BasicDBObject()
        fields.each { fieldName, fieldType ->
            def value
            if(String == fieldType) {
                value = Utils.randomString(length = 10)
            }
            if(Integer == fieldType) {
                value = Utils.randomNumber(max = 100)
            }
            document.append(fieldName, value)
        }
        collection.insert(document)
    }
}
