package com.ee.midas.soaktest

import com.ee.midas.soaktest.operations.DocumentInserter
import com.ee.midas.soaktest.operations.DocumentUpdater


def cli = new CliBuilder(usage: "Client --version=<v1>")
cli.with {
    _  args:1, argName: 'version', longOpt:'version', 'REQUIRED, client App version to run', required: true
}

def options = cli.parse(args)

def clientVersion = options.version

def configURL = new File("Config.groovy").toURI().toURL()

def config = new ConfigSlurper().parse(configURL)

def midasHost = config.data.mongoConnection.host
def midasPort = config.data.mongoConnection.port
def insertFrequency = config.data.pushFrequency.insert
def updateFrequency = config.data.pushFrequency.update

def dataInserter = new DocumentInserter(midasHost, midasPort, insertFrequency)
def dataUpdater = new DocumentUpdater(midasHost, midasPort, updateFrequency)


def databases = config.data.app.clientVersion.databases
databases.each { databaseName, collections ->
    collections.each { collectionName, documentSpec ->
        println("$databaseName, $collectionName, ${documentSpec.document}, ${documentSpec.count}")
        dataInserter.insertSampleDataFor(databaseName, collectionName, documentSpec.document, documentSpec.count)
    }
}

def updateAge = {
    document, fieldToUpdate ->
        def value = new Random().nextInt(100)
        document.put(fieldToUpdate, value)
}

dataUpdater.viewAndUpdateData("users", "customers", "age", updateAge)
dataUpdater.viewAndUpdateData("users", "customers", "customerID", {document, field -> document})
dataUpdater.viewAndUpdateData("transactions", "orders", "orderID", {document, field -> document})
