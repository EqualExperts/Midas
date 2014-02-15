package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.transform.TransformType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import scala.Function3
import scala.Unit
import scala.Tuple3

@Slf4j
public class Tree {
    @Delegate
    private final Context ctx = new Context()
    private final Map<String, Database> databases = [:]

    public Tree() {
    }

    @CompileStatic
    def using(String dbName) {
        def database = createOrGetDatabase(dbName)
        updateDB(database)
        database
    }

    @CompileStatic
    private Database createOrGetDatabase(String name) {
        if (databases.containsKey(name)) {
            log.info("Using database $name")
            databases[name]
        } else {
            log.info("Creating Database $name")
            databases[name] = new Database(name, ctx)
        }
    }

    /**
     *
     * @param transformType
     * @param closure with three params
     * 1. String dbName,
     * 2. String collectionName,
     * 3. Map versionedMap, keyed by version number (Long) for that transformType and values are:
     *    Tuple(verb, args, changeSet)
     *
     * @return
     */
    @CompileStatic
    def eachWithVersionedMap(TransformType transformType, Closure closure) {
        databases.each { name, Database database ->
            database.eachWithVersionedMap(transformType, closure)
        }
    }

    @CompileStatic
    void foreachDelta(TransformType transformType, Function3<String, String, scala.collection.mutable.Map<Double, Tuple3<Verb, List<String>, Long>>, Unit> fn) {
        databases.each { String name, Database database ->
            database.foreachDelta(transformType, fn)
        }
    }
}
