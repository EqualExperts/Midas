package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.dsl.interpreter.Parser
import com.ee.midas.dsl.interpreter.representation.Database
import com.ee.midas.dsl.interpreter.representation.Tree
import spock.lang.Specification

class ParserSpecs extends Specification  {

    def "it parses a database into a tree"() {
        given: "a new parser"
        Parser parser  = new Parser()

        when: "it parses a closure for database"
        parser.parse {
            -> using someDatabase
        }

        Tree tree = parser.ast()

        then: "the database is parsed and stored in a tree"
        tree.@databases.containsKey("someDatabase")
        tree.@databases.values().each {
            assert it instanceof Database
        }
    }

    def "it parses an expansion snippet into a tree"() {
        given: "a new parser"
        Parser parser  = new Parser()

        when: "it parses an expansion snippet within a closure"
        parser.parse { ->
            using someDatabase
            db.collectionName.add('{"newField" : "newValue"}')
        }

        Tree tree = parser.ast()

        then: "the generated ast must have parsed the db, collection with operation"
        tree.@databases.containsKey("someDatabase")
        tree.@databases.values().each {
            it instanceof Database
            it.@collections.values().each {
                it instanceof com.ee.midas.dsl.interpreter.representation.Collection
                println it
                it.@versionedExpansions.values().each {
                    def (operation, _) = it
                    assert operation == Grammar.add
                }
            }
        }
    }
}