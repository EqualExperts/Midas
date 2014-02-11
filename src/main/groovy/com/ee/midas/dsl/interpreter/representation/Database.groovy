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
    private final Context ctx

    @CompileStatic
    Database(String name, Context ctx) {
        this.name = name
        this.ctx = ctx
    }

    @CompileStatic
    def getProperty(String name) {
        Collection collection = null
        if(collections.containsKey(name)) {
            log.debug("Using Collection with $name")
            collection = collections[name]
        } else {
            log.info("Creating Collection $name")
            collection = collections[name] = new Collection(name, ctx)
        }
        collection
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
