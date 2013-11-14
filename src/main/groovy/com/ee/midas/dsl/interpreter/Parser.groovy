package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.interpreter.representation.Tree

class Parser {
    private def databases = new Tree()
    private def dbContext

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

    public Tree parse(Closure closure) {
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
