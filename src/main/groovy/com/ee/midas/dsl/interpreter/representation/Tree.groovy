package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType

public class Tree {
    private final def databases = [:]

    public Tree() {

    }

    def use(name) {
        println("Tree: use database with $name")
        if(databases.containsKey(name)){
            databases[name]
        }  else {
            println("Tree: Creating Database $name")
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
