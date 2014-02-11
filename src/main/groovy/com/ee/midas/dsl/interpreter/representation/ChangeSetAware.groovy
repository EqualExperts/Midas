package com.ee.midas.dsl.interpreter.representation

public interface ChangeSetAware {
    def updateCS(Long newChangeSet)
    def resetCS()
    def currentCS()
}