package com.ee.midas.dsl.interpreter.representation

import groovy.transform.ToString

@ToString
class Database {
    final String name
    private final def collections = [:]

    Database(String name) {
        this.name = name
    }

    def getProperty(String name) {
        println("Database: getProperty Collection with $name")
        def found = collections[name]
        if(!found)  {
            found = collections[name] = new Collection(name)
        }
        println("Database: $name, Returning Collection: $found")
        found
    }

    def each(Transform transform, closure) {
        def dbname = this.name
        collections.each { name, collection ->
            collection.each(transform, dbname, closure)
        }
    }
}
