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