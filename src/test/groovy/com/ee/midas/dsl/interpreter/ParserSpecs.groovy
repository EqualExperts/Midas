package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.dsl.interpreter.representation.Database
import com.ee.midas.dsl.interpreter.representation.Tree
import spock.lang.Specification

class ParserSpecs extends Specification  {

    def "it parses a database into a tree"() {
        given: 'a new parser'
            Parser parser = new Parser()

        when: 'it parses delta in a change set containing a database'
            def changeSet = 0
            parser.parse(changeSet) { ->
                using someDatabase
            }


        then: 'the database is parsed and stored in a intermediate representation'
            Tree tree = parser.ast()
            tree.currentDB() instanceof Database
    }

    def "it parses an expansion snippet into a tree"() {
        given: 'a new parser'
            Parser parser = new Parser()

        when: 'it parses delta in a change set containing an expansion snippet'
            def changeSet = 0
            parser.parse(changeSet) { ->
                using someDatabase
                db.collectionName.add('{"newField" : "newValue"}')
            }

        then: 'the generated ast must have parsed the db, collection with operation'
            Tree tree = parser.ast()
            def database = tree.currentDB()
            def collection = database.@collections['collectionName']
            def (operation, _) = collection.@versionedExpansions.values()[0]
            operation == Verb.add
    }
}