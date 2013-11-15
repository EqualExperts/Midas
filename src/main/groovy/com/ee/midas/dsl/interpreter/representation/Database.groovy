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
        if(collections.containsKey(name)) {
            collections[name]
        } else {
            println("Database: Creating Collection $name")
            collections[name] = new Collection(name)
        }
    }

    def each(Transform transform, closure) {
        def dbname = this.name
        collections.each { name, collection ->
            collection.each(transform, dbname, closure)
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name $collections"
    }
}
