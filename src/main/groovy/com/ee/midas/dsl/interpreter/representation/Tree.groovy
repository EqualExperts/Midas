package com.ee.midas.dsl.interpreter.representation

public class Tree {
    private final def databases = [:]

    public Tree() {

    }

    def use(name) {
        def found = databases[name]
        if(!found) {
            println("Creating database $name")
            found = databases[name] = new Database(name)
        }
        found
    }

    def each(Transform transform, closure) {
        databases.each { name, database ->
            database.each(transform, closure)
        }
    }
}
