package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import spock.lang.Specification

public class GrammarSpecs extends Specification {

    //Add
    def "'add' validates correct argument types"() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: 'the validation was successful'
            notThrown(InvalidGrammar)

        where:
            args << [
                ["{}"],
                ["""{"age" : 0 }"""],
                ["{\"age\" : 0 , \"address.zip\" : 400001}"]
            ]
    }

    def "'add' does not validate incorrect argument types"() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["{\"age\" : 0 }", "{\"address.zip\" : 400001}"],
                ["""age : 0"""],
                [1,2.0],
                [true, false]
            ]
    }

    //Remove
    def "'remove' validates correct argument types"() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation was successful'
            notThrown(InvalidGrammar)

        where:
            args << [
                ["{}"],
                ["[\"age\"]"],
                ["[\"age\", \"address.zip\"]"]
            ]
    }

    def "'remove' does not validate incorrect argument types"() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["age","address.zip"],
                ["[age : 0]"],
                [1,2.0],
                [true, false]
            ]
    }

    //Copy

    def "'copy' validates correct argument types"() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation was successful'
            notThrown(InvalidGrammar)

        where:
            args << [
                ["sourceField", "targetField"],
                ["source field", "target field"]
            ]
    }

    def "'copy' does not validate incorrect argument types"() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["singleField"],
                ["field1", "field2", "field3"],
                [1, 2.0],
                [true, false]
            ]
    }

    //split

    def "'split' validates correct argument types"() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation was successful'
            notThrown(InvalidGrammar)

        where:
            args << [
                ["sourceField", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"],
                ["source field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"]
            ]
    }

    def "'split' does not validate incorrect argument types"() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["sourceField"],
                ["sourceField", "targetField"],
                ["sourceField", "targetField", "targetField2"],
                [1, 2, 3],
                [true, true, false],
                ["{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}"],
                ["source field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}", "extraArg"]
            ]
    }


    //merge

    def "'mergeInto' validates correct argument types"() {
        given: 'mergeInto verb from Grammar'
            Grammar merge = Grammar.mergeInto

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation was successful'
            notThrown(InvalidGrammar)

        where:
            args << [
                ["targetField", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"],
                ["target field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"]
            ]
    }

    def "'mergeInto' does not validate incorrect argument types"() {
        given: 'mergeInto verb from Grammar'
            Grammar merge = Grammar.mergeInto

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["singleField"],
                ["targetField", "sourceField"],
                ["targetField", "sourceField1", "sourceField2"],
                [1, 2, 3],
                [true, true, false],
                ["{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}"],
                ["target field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}", "extraArg"]
            ]
    }

}
