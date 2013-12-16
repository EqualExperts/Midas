package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType
import groovy.util.logging.Slf4j

@Slf4j
public class Tree {
    private final def databases = [:]

    public Tree() {
    }

    def use(name) {
        if(databases.containsKey(name)){
            log.info("Using database $name")
            databases[name]
        }  else {
            log.info("Creating Database $name")
            databases[name] = new Database(name)
        }
    }

    def each(TransformType transformType, closure) {
        databases.each { name, Database database ->
            database.each(transformType, closure)
        }
    }

    def eachWithVersionedMap(TransformType transformType, closure) {
        databases.each { name, Database database ->
            database.eachWithVersionedMap(transformType, closure)
        }
    }
}
