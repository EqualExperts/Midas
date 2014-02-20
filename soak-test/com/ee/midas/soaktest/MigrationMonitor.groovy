package com.ee.midas.soaktest

import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.transform.TransformType
import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.Mongo
import com.ee.midas.dsl.interpreter.Reader
import groovy.transform.Field

def cli = new CliBuilder(usage: "Client --version=<v1>")
cli.with {
    _  args:1, argName: 'version', longOpt:'version', 'REQUIRED, client App version to run', required: true
}

def options = cli.parse(args)

def clientVersion = options.version

def configURL = new File("Config.groovy").toURI().toURL()
def config = new ConfigSlurper().parse(configURL)
def currentChangeSet = config.data.app."${clientVersion}".changeSet

def deltasDir = config.data.deltas.baseDir
def expansions = config.data.deltas.expansions
def contractions = config.data.deltas.contractions

def expansionFiles = []
def contractionFiles = []
expansions.each {
    deltaName, deltaContent ->
        expansionFiles.add(new File(toFileName(deltasDir, "app", currentChangeSet, "expansion", deltaName)))
}

contractions.each {
    deltaName, deltaContent ->
        contractionFiles.add(new File(toFileName(deltasDir, "app", currentChangeSet, "contraction", deltaName)))
}

Reader reader = new Reader()
Tree expansionTree = reader.read(expansionFiles)
Tree contractionTree = reader.read(contractionFiles)

def expansionVersionChgSetMap = mapChangeSetToTransformTypeVersion(TransformType.EXPANSION, expansionTree)

def contractionVersionChgSetMap = mapChangeSetToTransformTypeVersion(TransformType.CONTRACTION, contractionTree)


def databases = config.data.app."$clientVersion".databases
databases.each { databaseName, collections ->
    collections.each { collectionName, documentSpec ->
        def namespace = "$databaseName.$collectionName"
        def key = new Tuple(currentChangeSet, namespace)
        println("looping through deltas...")
        def maxExpansionVersion = expansionVersionChgSetMap[key]
        def maxContractionVersion = contractionVersionChgSetMap[key]

        checkUntilExpansionComplete(databaseName, collectionName, maxExpansionVersion)

        println("maxExpansionVersion = $maxExpansionVersion")
        println("maxContractionVersion = $maxContractionVersion")
        println("$databaseName, $collectionName, ${documentSpec.document}, ${documentSpec.count}")
    }
}

private def mapChangeSetToTransformTypeVersion(TransformType transformType, Tree tree) {
    println("Started Mapping ChangeSet to transformations for $transformType TransformType...")
    def request = [:]
    tree.eachWithVersionedMap(transformType) { String dbName, String collectionName, Map versionedMap ->
        def fullCollectionName = toFullCollectionName(dbName, collectionName)
        request << versionedMap.collectEntries { Double version, Tuple values ->
            def (verb, args, changeSet) = values
            [new Tuple(changeSet, fullCollectionName), version]
        }
    }
    println("Completed Mapping ChangeSet to Transforms versions for $transformType TransformType!")
    request
}

private def toFullCollectionName(String dbName, String collectionName) {
    "$dbName.$collectionName"
}

def checkUntilExpansionComplete(def databaseName, def collectionName, def maxExpansionVersion) {
    def mongoHost = "localhost"
    def mongoPort = 27017
    def mongo = new Mongo(mongoHost, mongoPort)
    DB db = mongo.getDB(databaseName)
    DBCollection collection = db.getCollection(collectionName)
    def totalDocuments = collection.count()
    def expandedDocuments = 0
    while(expandedDocuments < totalDocuments) {
        println("Expansion in progress for $databaseName.$collectionName. $expandedDocuments out of $totalDocuments expanded so far.")
        println("Will check again in 10 secs.")
        Thread.sleep(10000)
		totalDocuments = collection.count()
        expandedDocuments = collection.count(new BasicDBObject("_expansionVersion" : maxExpansionVersion))
    }
    println("Expansion complete for $databaseName.$collectionName.")
}

String toFileName(def deltasDir, String appName, def changeSet, String mode, def deltaName) {
    def pathSeparator = File.separator
    return "${deltasDir}${pathSeparator}${appName}${pathSeparator}${changeSet}${pathSeparator}${mode}${pathSeparator}${deltaName}"
}