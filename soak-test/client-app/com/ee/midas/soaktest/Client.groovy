package com.ee.midas.soaktest

import com.ee.midas.soaktest.operations.DataInserter
import com.ee.midas.soaktest.operations.DataUpdater

def dataUpdater = new DataUpdater()
def dataInserter = new DataInserter()

def configURL = new File("Config.groovy").toURI().toURL()
def config = new ConfigSlurper().parse(configURL)
def databases = config.soakdata.databases
databases.each {
    databaseName, collections -> collections.each {
        collectionName, attributes ->
            fields = attributes['fields']
            numOfDocuments = attributes['numOfDocuments']
            println("$databaseName, $collectionName, $fields, $numOfDocuments")
            dataInserter.insertSampleDataFor(databaseName, collectionName, fields, numOfDocuments)
    }
}

def updateAge = {
    document, fieldToUpdate ->
        def value = new Random().nextInt(100)
        document.put(fieldToUpdate, value)
}

dataUpdater.viewAndUpdateData("users", "customers", "age", updateAge)
dataUpdater.viewAndUpdateData("users", "customers", "customerID", {document, field -> document})

