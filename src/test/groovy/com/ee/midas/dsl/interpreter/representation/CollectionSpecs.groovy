package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.transform.TransformType
import spock.lang.Specification

class CollectionSpecs extends Specification {

    def "it accepts valid expansion operations"() {
        given: "A Collection"
            Collection collection = new Collection("testCollection")

        when: "a few valid expansion operations are invoked on it"
            collection.add("{\"newField\":\"defaultValue\"}")
            collection.copy("sourceField","targetField")
            collection.mergeInto("targetField", "separator", "[\"field1\", \"field2\"]")
            collection.split("sourceField", "regex", "{\"field1\":\"\$1\",\"field2\":\"\$2\"}")

        then: "the operations were successful"
            notThrown(InvalidGrammar)
    }

    def "it accepts valid contraction operation"() {
        given: "A Collection"
            Collection collection = new Collection("testCollection")

        when: "a contraction operation 'remove' is invoked with correct args"
            collection.remove("[\"field1\", \"field2\"]")

        then: "the operation was successful"
            notThrown(InvalidGrammar)

    }

    def "it throws an exception for unknown operations"() {
        given: "A Collection"
            Collection collection = new Collection("testCollection")

        when: "An unknown method is invoked on it"
            collection.unknownMethod(args)

        then: "It throws invalid grammar exception"
            thrown(InvalidGrammar)

        where:
            args << [
                    '{"age":0}',
                    '',
                    null
            ]

    }

    def "it throws an exception for operations with no args"() {
        given: "A Collection"
           Collection collection = new Collection("testCollection")

        when: "An operation with no arguments is invoked on it"
            collection.remove()

        then: "Invalid Grammar exception is thrown"
            thrown(InvalidGrammar)

    }

    def "it creates a versioned map for single EXPANSION"() {
        given: "A Collection with expansion operation"
            Collection collection = new Collection("testCollection")
            collection.add('{"age":0}')

        when: "it creates a versioned Map for EXPANSION type"
            LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

        then: "the map should contain operation of expansion type"
            LinkedHashMap expectedMap  = ["${1}":[name: Grammar.add.name(), args:['{"age":0}']]]
            versionedMap.equals(expectedMap)
    }

    def "it creates a versioned map for single CONTRACTION"() {
        given: "A Collection with contraction operation"
            Collection collection = new Collection("testCollection")
            collection.remove('{"age":0}')

        when: "it creates a versioned Map for CONTRACTION type"
            LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then: "the map should contain operation of contraction type"
            LinkedHashMap expectedMap  = ["${1}":['name':Grammar.remove.name(), 'args':['{"age":0}']]]
            versionedMap.equals(expectedMap)
    }

    def "it creates a versioned map for multiple EXPANSIONs"() {
        given: "A Collection with multiple expansion operations"
            Collection collection = new Collection("testCollection")
            collection.add('{"age":0}')
            collection.add('{"city":"pune"}')

        when: "it creates a versioned Map for EXPANSION type"
            LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

        then: "the map should contain operations of expansion type"
            LinkedHashMap expectedMap  = ["${1}":['name':Grammar.add.name(),
                                                  'args':['{"age":0}']],
                                          "${2}":['name':Grammar.add.name(),
                                                  'args':['{"city":"pune"}']]]
            versionedMap.equals(expectedMap)
    }

    def "it creates a versioned map for multiple CONTRACTIONs"() {
        given: "A Collection with multiple contraction operations"
            Collection collection = new Collection("testCollection")
            collection.remove('{"age":0}')
            collection.remove('{"city":"pune"}')

        when: "it creates a versioned Map for CONTRACTION type"
            LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then: "the map should contain operations of contraction type"
            LinkedHashMap expectedMap  = ["${1}":['name':Grammar.remove.name(),
                                                  'args':['{"age":0}']],
                                          "${2}":['name':Grammar.remove.name(),
                                                  'args':['{"city":"pune"}']]]
            versionedMap.equals(expectedMap)
    }

    def "it ignores contraction operations when versioning EXPANSIONs"() {
        given: "A Collection with few expansion and contraction operations"
            Collection collection = new Collection("testCollection")
            collection.add('{"age":0}')
            collection.add('{"city":"pune"}')
            collection.remove('["dob"]')
            collection.remove('["city"]')

        when: "it creates a versioned Map for EXPANSION type"
            LinkedHashMap versionedExpansionMap = collection.asVersionedMap(TransformType.EXPANSION)

        then: "the map should contain operations of expansion type only"
            LinkedHashMap expectedExpansionMap = [
                    "${1}" : ['name': Grammar.add.name(),
                         'args': ['{"age":0}']],
                    "${2}" : ['name': Grammar.add.name(),
                         'args':['{"city":"pune"}']]
                    ]

            versionedExpansionMap.equals(expectedExpansionMap)
    }

    def "it ignores expansion operations when versioning CONTRACTIONs"() {
        given: "A Collection with few expansion and contraction operations"
            Collection collection = new Collection("testCollection")
            collection.add('{"age":0}')
            collection.add('{"city":"pune"}')
            collection.remove('["dob"]')
            collection.remove('["city"]')

        when: "it creates a versioned Map for CONTRACTION type"
            LinkedHashMap versionedContractionMap = collection.asVersionedMap(TransformType.CONTRACTION)


        then: "the map should contain operations of contraction type only"
            LinkedHashMap expectedContractionMap  = [
                    "${1}" : ['name': Grammar.remove.name(),
                            'args': ['["dob"]']],
                    "${2}" : ['name': Grammar.remove.name(),
                            'args': ['["city"]']]
            ]
            versionedContractionMap.equals(expectedContractionMap)

    }

}


