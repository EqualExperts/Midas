package com.ee.midas.dsl.interpreter.representation

import groovy.util.logging.Slf4j

@Slf4j
class Context implements ChangeSetAware, DatabaseAware {
    private Long cs = 0
    private Database db

    @Override
    def updateCS(Long cs) {
        this.cs = cs
    }

    @Override
    def resetCS() {
        cs = 0
    }

    @Override
    def currentCS() {
        cs
    }

    @Override
    def currentDB() {
        db
    }

    @Override
    def updateDB(Database db) {
        this.db = db
    }
}
