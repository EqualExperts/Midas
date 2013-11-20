package com.ee.midas.dsl.interpreter.representation

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

    def each(Transform transform, closure) {
        databases.each { name, Database database ->
            database.each(transform, closure)
        }
    }

    def eachWithVersionedMap(Transform transform, closure) {
        databases.each { name, Database database ->
            database.eachWithVersionedMap(transform, closure)
        }
    }
}
