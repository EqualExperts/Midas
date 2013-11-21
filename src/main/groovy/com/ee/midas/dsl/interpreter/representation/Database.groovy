package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType
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

    def each(TransformType transformType, closure) {
        def dbName = this.name
        collections.each { name, Collection collection ->
            collection.each(transformType, dbName, closure)
        }
    }

    def eachWithVersionedMap(TransformType transformType, closure) {
        def dbName = this.name
        collections.each { name, Collection collection ->
            closure(dbName, name, collection.asVersionedMap(transformType))
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name $collections"
    }
}
