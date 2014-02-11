package com.ee.midas.dsl.interpreter.representation

interface DatabaseAware {
    def currentDB()
    def updateDB(Database db)
}
