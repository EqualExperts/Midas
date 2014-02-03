package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import com.ee.midas.dsl.interpreter.representation.Tree
import org.codehaus.groovy.control.CompilationFailedException
import spock.lang.Shared
import spock.lang.Specification

class ReaderSpecs extends Specification{

    @Shared def deltaFilesDir = "deltaFilesDirForReader"
    @Shared File baseDir
    def setupSpec() {
        baseDir = new File(deltaFilesDir)
        baseDir.mkdir()
    }

    def cleanupSpec() {
        baseDir.deleteDir()
    }

    def "reads from single delta file"() {
        given: "A delta file"
            def deltaStr = """
                             use users
                             db.customers.add('{"age" : 0}')
                           """
            createDelta("001_add.delta", deltaStr)

        and: "A reader"
            def reader = new Reader()

        when: "Reader reads the delta"
            def result = reader.read(baseDir.listFiles().toList())

        then: "Reading was completed successfully"
            notThrown(Exception)
            result instanceof Tree
    }

    def "reads multiple delta files"() {
        given: "Multiple delta files"
            def mergeDeltaStr = """
                                  use users
                                  db.customers.merge("['fName', 'lName']", " ", "name")
                                """

            def copyDeltaStr = """
                                 use users
                                 db.customers.copy("pin", "zip")
                               """
            createDelta("001_merge.delta", mergeDeltaStr)
            createDelta("002_copy.delta", copyDeltaStr)

        and: "A reader"
            def reader = new Reader()

        when: "Reader reads the delta"
            def result = reader.read(baseDir.listFiles().toList())

        then: "Reading was completed successfully"
            notThrown(Exception)
            result instanceof Tree
    }

    def "shouts if a file doesn't exist"() {
        given: "A Reader"
            def reader = new Reader()

        when: "it tries to read a file that doesn't exist"
            reader.read([new File("nonExistingFile.delta")])

        then: "a FileNotFoundException is thrown"
            thrown(FileNotFoundException)
    }

    def "rejects a json value not presented as a string in delta"() {
        given: "A delta file with json value"
            def addDeltaStr = """
                                use users
                                db.customers.add({"age" : 0})
                              """
            createDelta("001_add_field_age.delta", addDeltaStr)

        and: "A reader"
            def reader = new Reader()

        when: "Reader reads the delta"
            reader.read(baseDir.listFiles().toList())

        then: "Compilation of delta fails"
            thrown(CompilationFailedException)
    }

    def "bubbles up grammar errors within a delta"() {
        given: "A delta with no parameter"
            def addDeltaStr = """
                                use users
                                db.customers.add()
                              """
            createDelta("001_add_field_age.delta", addDeltaStr)

        and: "A reader"
            def reader = new Reader()

        when: "Reader reads the delta"
            reader.read(baseDir.listFiles().toList())

        then: "Invalid grammar exception was bubbled up"
            thrown(InvalidGrammar)
    }

    private def createDelta(String fileName, String contents) {
        new File("$deltaFilesDir/$fileName").write(contents)
    }
}
