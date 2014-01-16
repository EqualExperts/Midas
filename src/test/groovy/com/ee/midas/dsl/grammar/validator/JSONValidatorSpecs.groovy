package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import spock.lang.Specification

public class JSONValidatorSpecs extends Specification {

    def itValidatesValidJSON() {
        given: 'a validator with error message set'
            def errMsg = "%s is an invalid json: %s"
            def validator = new JSONValidator(errMsg)

        expect: 'verb to validate successfully the arguments supplied'
            validator.validate(arg)

        where:
            arg << [
                '{}',
                "{}",
                '[]',
                "[]",
                '{"age" : 0 }',
                "{'age' : 0 }",
                "{ \$add : [ '\$age', 1] }",
                '{ $add : [ "$age", 1] }',
                "{ \$subtract: [10, 2] }"
            ]

    }

    def itDoesNotValidateInvalidJSON() {
        given: 'a validator with error message set'
            def errMsg = "%s is an invalid json: %s"
            def validator = new JSONValidator(errMsg)

        when: 'verb to validate successfully the arguments supplied'
            validator.validate(arg)

        then: ''
            def problem = thrown(InvalidGrammar)
            problem.message.contains("$arg is an invalid json:")

        where:
            arg << [
                    '{',
                    "}",
                    '[',
                    "]",
                    '{"age : 0 }',
                    "{'age : 0 }",
                    "{ \$add : [ '\$age' 1] }",
                    '{ $add : [ "$age", ] }',
                    "{ \$subtract: [10,  }"
        ]
    }

}
