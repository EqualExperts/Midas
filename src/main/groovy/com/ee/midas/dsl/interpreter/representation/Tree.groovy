package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
public class Tree {
    @Delegate
    private final Context ctx = new Context()
    private final Map<String, Database> databases = [:]

    public Tree() {
    }

    @CompileStatic
    def using(String name) {
        Database database = null
        if(databases.containsKey(name)){
            log.info("Using database $name")
            database = databases[name]
        }  else {
            log.info("Creating Database $name")
            database = databases[name] = new Database(name, ctx)
        }
        updateDB(database)
    }

    @CompileStatic
    def eachWithVersionedMap(TransformType transformType, Closure closure) {
        databases.each { name, Database database ->
            database.eachWithVersionedMap(transformType, closure)
        }
    }
}
