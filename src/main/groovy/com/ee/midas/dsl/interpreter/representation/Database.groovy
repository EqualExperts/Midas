package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.transform.TransformType
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import scala.Function3
import scala.Unit
import scala.Tuple3


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
        if(collections.containsKey(name)) {
            log.debug("Using Collection with $name")
            collections[name]
        } else {
            log.info("Creating Collection $name")
            collections[name] = new Collection(name, ctx)
        }
    }



    @CompileStatic
    def eachWithVersionedMap(TransformType transformType, Closure closure) {
        def dbName = this.name
        collections.each { String name, Collection collection ->
            closure(dbName, name, collection.asVersionedMap(transformType))
        }
    }

    @CompileStatic
    void foreachDelta(TransformType transformType, Function3<String, String, scala.collection.mutable.Map<Double, Tuple3<Verb, List<String>, Long>>, Unit> fn) {
        def dbName = this.name
        collections.each { String name, Collection collection ->
            fn.apply(dbName, name, collection.asVersionedScalaMap(transformType))
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name $collections"
    }
}
