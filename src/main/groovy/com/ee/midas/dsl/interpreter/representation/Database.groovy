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
        def dbName = this.name
        collections.each { name, Collection collection ->
            collection.each(transform, dbName, closure)
        }
    }

    def eachWithVersionedMap(Transform transform, closure) {
        def dbName = this.name
        collections.each { name, Collection collection ->
            closure(dbName, name, collection.asVersionedMap(transform))
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name $collections"
    }
}
