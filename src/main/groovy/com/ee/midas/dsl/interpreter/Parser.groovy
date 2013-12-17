package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.interpreter.representation.Tree
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
class Parser {
    private Tree tree = new Tree()
    private def dbContext

    @CompileStatic
    def getProperty(String name) {
        log.debug("property name is: $name")
        if(name == 'db') {
            return dbContext
        }
        tree.using(name)
    }

    def using(db) {
        log.info "Setting db context to ${db.toString()}"
        dbContext = db
    }

    public Tree parse(Closure closure) {
        def cloned = closure.clone()
        cloned.delegate = this
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned()
        tree
    }

    def ast() {
        tree
    }
}
