package com.ee.midas.dsl.interpreter.representation

public class Databases {
    private def dbs = [:]

    public Databases() {

    }

    def use(name) {
        def found = dbs[name]
        if(!found) {
            println("Creating database $name")
            found = dbs[name] = new Database(name)
        }
        found
    }

    def each(Transform transform, closure) {
        dbs.each { name, database ->
            database.each(transform, closure)
        }
    }
}
