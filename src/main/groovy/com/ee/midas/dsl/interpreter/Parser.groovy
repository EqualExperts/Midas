package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.interpreter.representation.Databases

public class Parser {
    private def databases = new Databases()
    private def dbContext

    public Parser() {
    }

    def getProperty(String name) {
        if(name == 'db') {
            return dbContext
        }
        databases.use(name)
    }

    def using(db) {
        println "Parser: Setting db context to ${db.toString()}"
        dbContext = db
    }

    def Databases parse(Closure closure) {
        def cloned = closure.clone()
        cloned.delegate = this
//        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned()
        databases
    }

    def ast() {
        databases
    }
}
