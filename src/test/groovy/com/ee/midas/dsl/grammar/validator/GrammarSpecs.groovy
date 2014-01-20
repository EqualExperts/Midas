package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import spock.lang.Specification

public class GrammarSpecs extends Specification {

    //Add
    def itValidatesSingleAddArgumentTypeAsJSON() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add
            def args = ["""{"age" : 0 }"""]

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then:
            notThrown(InvalidGrammar)
    }

    def itValidatesMultipleAddFieldsWithinSingleArgumentTypesAsJSON() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add

        and: 'a list of 2 add fields within single JSON'
            def args = ["{\"age\" : 0 , \"address.zip\" : 400001}"]

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    def itDoesNotValidatesMultipleAddArgumentTypesAsJSON() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add

        and: 'a list of 2 string JSONs '
            def args = ["{\"age\" : 0 }", "{\"address.zip\" : 400001}"]

        when: "the verb validates the arguments supplied"
            add.validate(args)

        then: "the validation fails"
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateAddArgumentTypeContainingInvalidJSON() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add
            def args = ["""age : 0"""]

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: "the validation fails"
            thrown(InvalidGrammar)
    }


    def itDoesNotValidateNumericAddArgumentType() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add

        and: 'a list of numeric arguments'
            List args = [1,2.0]

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateBooleanAddArgumentType() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add

        and: 'a list of boolean arguments'
            List args = [true, false]

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    //Remove

    def itValidatesSingleRemoveArgumentTypeAsJSON() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        and: 'a single json argument'
            def args = ["[\"age\"]"]

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    def itValidatesMultipleRemoveFieldsWithinSingleArgumentTypesAsJSON() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        and: 'a single json with 2 arguments'
            def args = ["[\"age\", \"address.zip\"]"]

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    def itDoesNotValidatesMultipleRemoveArgumentTypesAsJSON() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        and: '2 json arguments'
            def args = ["[\"age\"]","[\"address.zip\"]"]

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateRemoveArgumentTypeAsInvalidJSON() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        and: 'an invalid json'
            def args = ["[age : 0]"]

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateNumericRemoveArgumentType() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        and: 'a list of numeric arguments'
            List args = [1,2.0]

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateBooleanRemoveArgumentType() {
        given: 'Remove verb from Grammar'
            Grammar remove = Grammar.remove

        and: 'a list of boolean arguments'
            List args = [true, false]

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    //Copy
    def itDoesNotValidateSingleCopyArgumentType() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        and: 'a single string argument as a list'
            def args = ["sourceField"]

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itValidatesTwoCopyArgumentTypes() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        and: 'a list of 2 string arguments'
            def args = ["sourceField", "targetField"]

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    def itValidatesTwoCopyArgumentTypesWithSpaces() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        and: 'a list of 2 string arguments containing spaces'
            def args = ["source field", "target field"]

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    def itDoesNotValidateMoreThanTwoCopyArgumentTypes() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        and: 'a list of 3 string arguments'
            def args = ["field1", "field2", "field3"]

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateNumericCopyArgumentTypes() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        and: 'a list of numeric arguments'
            List args = [1, 2.0]

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    def itDoesNotValidateBooleanCopyArgumentTypes() {
        given: 'Copy verb from Grammar'
            Grammar copy = Grammar.copy

        and: 'a list of boolean arguments'
            List args = [true, false]

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }


    //split
    def itDoesNotValidateSingleSplitArgumentType() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a single string argument as list'
            def args = ["sourceField"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateTwoSplitArgumentTypes() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 2 string arguments'
            def args = ["sourceField", "targetField"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeSplitArgumentsOfStringTypes() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 3 string arguments'
            def args = ["sourceField", "targetField", "targetField2"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeSplitArgumentsOfNumericTypes() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 3 numeric arguments'
            List args = [1, 2, 3]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeSplitArgumentsOfBooleanTypes() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 3 boolean arguments'
            List args = [true, true, false]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeSplitArgumentsOfJSONTypes() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 3 json arguments'
            List args = ["{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itValidatesThreeSplitArgumentsOfTypesStringStringAndJSON() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 3 string arguments with 3rd string as json'
            def args = ["sourceField", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    
    def itValidatesSourceFieldOfSplitArgumentTypesWithSpaces() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 3 valid arguments, with source field containing space'
            def args = ["source field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    
    def itDoesNotValidateMoreThanThreeSplitArgumentTypes() {
        given: 'Split verb from Grammar'
            Grammar split = Grammar.split

        and: 'a list of 4 arguments'
            def args = ["source field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}", "extraArg"]

        when: 'the verb validates the arguments supplied'
            split.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    //merge
    
    def itDoesNotValidateSingleMergeArgumentType() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a single string as list'
            def args = ["sourceField"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateTwoMergeArgumentTypes() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 2 strings'
            def args = ["sourceField", "targetField"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeMergeArgumentsOfStringTypes() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 3 strings'
            def args = ["sourceField", "targetField", "targetField2"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeMergeArgumentsOfNumericTypes() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 3 numeric arguments'
            List args = [1, 2, 3]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeMergeArgumentsOfBooleanTypes() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 3 boolean arguments'
            List args = [true, true, false]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itDoesNotValidateThreeMergeArgumentsOfJSONTypes() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 3 json arguments'
            List args = ["{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

    
    def itValidatesThreeMergeArgumentsOfTypesStringStringAndJSON() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 3 string args, with 3rd one as json'
            def args = ["sourceField", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    
    def itValidatesTargetFieldOfMergeArgumentTypesWithSpaces() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of valid arguments with target field name containing spaces'
            def args = ["target field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'no exception was thrown on validation'
            notThrown(InvalidGrammar)
    }

    
    def itDoesNotValidateMoreThanThreeMergeArgumentTypes() {
        given: 'Merge verb from Grammar'
            Grammar merge = Grammar.mergeInto

        and: 'a list of 4 arguments'
            def args = ["source field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}", "extraArg"]

        when: 'the verb validates the arguments supplied'
            merge.validate(args)
        then: 'the validation fails'
            thrown(InvalidGrammar)
    }

}
