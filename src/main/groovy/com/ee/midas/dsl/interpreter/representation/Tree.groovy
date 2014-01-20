package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
public class Tree {
    private final Map<String, Database> databases = [:]

    public Tree() {
    }

    @CompileStatic
    def using(String name) {
        if(databases.containsKey(name)){
            log.info("Using database $name")
            databases[name]
        }  else {
            log.info("Creating Database $name")
            databases[name] = new Database(name)
        }
    }

    @CompileStatic
    def eachWithVersionedMap(TransformType transformType, Closure closure) {
        databases.each { name, Database database ->
            database.eachWithVersionedMap(transformType, closure)
        }
    }
}
