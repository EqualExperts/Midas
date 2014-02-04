package com.ee.midas.soaktest

import com.ee.midas.soaktest.operations.Utils

def configURL = new File("Config.groovy").toURI().toURL()
def config = new ConfigSlurper().parse(configURL)
def databases = config.soakdata.databases
databases.each {
    databaseName, collections -> collections.each {
        collectionName, attributes ->
            fields = attributes['fields']
            numOfDocuments = attributes['numOfDocuments']
            (1..numOfDocuments).each {

                fields.each {
                    fieldName, fieldType ->
                        if(String.class.equals(fieldType)) {
                            def value = Utils.randomString(length = 10)
                            println("$databaseName, $collectionName, $fieldName, $value, $numOfDocuments")
                        }
                        if(Integer.class.equals(fieldType)) {
                            def value = Utils.randomNumber(max = 100)
                            println("$databaseName, $collectionName, $fieldName, $value, $numOfDocuments")
                        }
                }
            }
    }
}