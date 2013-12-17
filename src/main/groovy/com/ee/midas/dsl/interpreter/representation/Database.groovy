package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString
@Slf4j
class Database {
    final String name
    private final Map<String, Collection> collections = [:]

    @CompileStatic
    Database(String name) {
        this.name = name
    }

    @CompileStatic
    def getProperty(String name) {
        if(collections.containsKey(name)) {
            log.debug("Using Collection with $name")
            collections[name]
        } else {
            log.info("Creating Collection $name")
            collections[name] = new Collection(name)
        }
    }

    @CompileStatic
    def each(TransformType transformType, Closure closure) {
        def dbName = this.name
        collections.each { name, Collection collection ->
            collection.each(transformType, dbName, closure)
        }
    }

    @CompileStatic
    def eachWithVersionedMap(TransformType transformType, Closure closure) {
        def dbName = this.name
        collections.each { String name, Collection collection ->
            closure(dbName, name, collection.asVersionedMap(transformType))
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name $collections"
    }
}
