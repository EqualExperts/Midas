/******************************************************************************
 * Copyright (c) 2014, Equal Experts Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the Midas Project.
 ******************************************************************************/

package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import spock.lang.Specification

public class VerbSpecs extends Specification {

    //Add
    def "'add' validates correct argument types"() {
        given: 'Add verb from Grammar'
            Verb add = Verb.add

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
            Verb add = Verb.add

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["{\"age\" : 0 }", "{\"address.zip\" : 400001}"],
                ["""age : 0"""]
            ]
    }

    //Remove
    def "'remove' validates correct argument types"() {
        given: 'Remove verb from Grammar'
            Verb remove = Verb.remove

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
            Verb remove = Verb.remove

        when: 'the verb validates the arguments supplied'
            remove.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["age","address.zip"],
                ["[age : 0]"]
            ]
    }

    //Copy

    def "'copy' validates correct argument types"() {
        given: 'Copy verb from Grammar'
            Verb copy = Verb.copy

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
            Verb copy = Verb.copy

        when: 'the verb validates the arguments supplied'
            copy.validate(args)

        then: 'the validation fails'
            thrown(InvalidGrammar)

        where:
            args << [
                [],
                ["singleField"],
                ["field1", "field2", "field3"]
            ]
    }

    //split

    def "'split' validates correct argument types"() {
        given: 'Split verb from Grammar'
            Verb split = Verb.split

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
            Verb split = Verb.split

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
                ["{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}"],
                ["source field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}", "extraArg"]
            ]
    }


    //merge

    def "'merge' validates correct argument types"() {
        given: 'merge verb from Grammar'
            Verb merge = Verb.merge

        when: 'the verb validates the arguments supplied'
            merge.validate(args)

        then: 'the validation was successful'
            notThrown(InvalidGrammar)

        where:
            args << [
                ["{\"field1\": \"\$1\", \"field2\": \"\$2\"}", " ", "targetField"],
                ["{\"field1\": \"\$1\", \"field2\": \"\$2\"}", " ", "target field"]
            ]
    }

    def "'merge' does not validate incorrect argument types"() {
        given: 'merge verb from Grammar'
            Verb merge = Verb.merge

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
                ["{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}"],
                ["target field", " ", "{\"field1\": \"\$1\", \"field2\": \"\$2\"}", "extraArg"]
            ]
    }

}
