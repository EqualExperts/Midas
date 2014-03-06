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

package com.ee.midas.dsl.interpreter.representation

import static com.ee.midas.dsl.grammar.Verb.*
import com.ee.midas.transform.TransformType
import spock.lang.Specification

class CollectionSpecs extends Specification {

    def "it accepts valid expansion operations"() {
        given: "A Collection with context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)

        when: "a few valid expansion operations are invoked on it"
            collection.add("{\"newField\":\"defaultValue\"}")
            collection.copy("sourceField","targetField")
            collection.merge("[\"field1\", \"field2\"]", "separator", "targetField")
            collection.split("sourceField", "regex", "{\"field1\":\"\$1\",\"field2\":\"\$2\"}")

        then: "the operations were successful"
            notThrown(InvalidGrammar)
    }

    def "it accepts valid contraction operation"() {
        given: "A Collection with context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)

        when: "a contraction operation 'remove' is invoked with correct args"
            collection.remove("[\"field1\", \"field2\"]")

        then: "the operation was successful"
            notThrown(InvalidGrammar)

    }

    def "it throws an exception for unknown operations"() {
        given: "A Collection with context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)

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

    def "it throws an exception for valid operations with no args"() {
        given: "A Collection with context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)

        when: "An operation with no arguments is invoked on it"
            collection.remove()

        then: "Invalid Verb exception is thrown"
            thrown(InvalidGrammar)

    }

    def "it creates a versioned map for single EXPANSION"() {
        given: "A Collection with expansion operation in context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)
            collection.add('{"age":0}')

        when: "it creates a versioned Map for EXPANSION type"
            def versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

        then: "the map should contain operation of expansion type"
            def expectedMap = [1L: new Tuple(add, ['{"age":0}'], 0)]
            versionedMap.size() == expectedMap.size()
            versionedMap[1] == expectedMap[1]
    }

    def "it creates a versioned map for single CONTRACTION"() {
        given: "A Collection with contraction operation in context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)
            collection.remove('{"age":0}')

        when: "it creates a versioned Map for CONTRACTION type"
            def versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then: "the map should contain operation of contraction type"
            def expectedMap  = [1L: new Tuple(remove, ['{"age":0}'], 0)]
            versionedMap.size() == expectedMap.size()
            versionedMap[1] == expectedMap[1]
    }

    def "it creates a versioned map for multiple EXPANSIONs"() {
        given: "A Collection with multiple expansion operations in context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)
            collection.add('{"age":0}')
            collection.add('{"city":"pune"}')

        when: "it creates a versioned Map for EXPANSION type"
            def versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

        then: "the map should contain operations of expansion type"
            def expectedMap = [
                1d: new Tuple(add, ['{"age":0}'], 0),
                2d: new Tuple(add, ['{"city":"pune"}'], 0)
            ]
        versionedMap.size() == expectedMap.size()
        expectedMap.each { version, value ->
            assert versionedMap[version] == value
        }
    }

    def "it creates a versioned map for multiple CONTRACTIONs"() {
        given: "A Collection with multiple contraction operations in context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)
            collection.remove('{"age":0}')
            collection.remove('{"city":"pune"}')

        when: "it creates a versioned Map for CONTRACTION type"
            def versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then: "the map should contain operations of contraction type"
            def expectedMap  = [
                    1d: new Tuple(remove, ['{"age":0}'], 0),
                    2d: new Tuple(remove, ['{"city":"pune"}'], 0)
            ]
            versionedMap.size() == expectedMap.size()
            expectedMap.each { version, value ->
                assert versionedMap[version] == value
            }
    }

    def "it ignores contraction operations when versioning EXPANSIONs"() {
        given: "A Collection with few expansion and contraction operations in context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)
            collection.add('{"age":0}')
            collection.add('{"city":"pune"}')
            collection.remove('["dob"]')
            collection.remove('["city"]')

        when: "it creates a versioned Map for EXPANSION type"
            def versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

        then: "the map should contain operations of expansion type only"
            def expectedMap = [
                    1d: new Tuple(add, ['{"age":0}'], 0),
                    2d: new Tuple(add, ['{"city":"pune"}'], 0)
            ]
            versionedMap.size() == expectedMap.size()
            expectedMap.each { version, value ->
                assert versionedMap[version] == value
            }
    }

    def "it ignores expansion operations when versioning CONTRACTIONs"() {
        given: "A Collection with few expansion and contraction operations in context"
            def ctx = new Context()
            Collection collection = new Collection("testCollection", ctx)
            collection.add('{"age":0}')
            collection.add('{"city":"pune"}')
            collection.remove('["dob"]')
            collection.remove('["city"]')

        when: "it creates a versioned Map for CONTRACTION type"
            def versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then: "the map should contain operations of contraction type only"
            def expectedMap  = [
                    1d: new Tuple(remove, ['["dob"]'], 0),
                    2d: new Tuple(remove, ['["city"]'], 0)
            ]
            versionedMap.size() == expectedMap.size()
            expectedMap.each { version, value ->
                assert versionedMap[version] == value
            }

    }

}


