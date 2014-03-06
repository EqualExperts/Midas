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

import com.ee.midas.transform.TransformType
import spock.lang.Specification

class DatabaseSpecs extends Specification {

   def "adds a single collection to a database tree"() {
        given: "A Database with context"
            def ctx = new Context()
            Database database = new Database("testDB", ctx)

        when: "a collection is added to it"
            def collection = database.testCollection

        then: "the collection gets added to database"
            collection  == database.getProperty("testCollection")
   }

    def "adds multiple collections to a database tree"() {
        given: "A Database with context"
            def ctx = new Context()
            Database database = new Database("testDB", ctx)

        when: "a multiple collections are added to it"
            def collection1 = database.testCollection1
            def collection2 = database.testCollection2

        then: "the collections get added to the database"
            collection1  == database.getProperty("testCollection1")
            collection2  == database.getProperty("testCollection2")
    }

    def "does not version a database with no collections"() {
        given: "A database with no collection and context"
            def ctx = new Context()
            Database database = new Database("testDB", ctx)
            def versionedDatabase = false

        when: "the database is versioned"
            database.eachWithVersionedMap(TransformType.EXPANSION) {
                versionedDatabase = true
            }

        then: "versioning was unsuccessful"
            versionedDatabase == false
    }

    def "versioning a database rejects a closure with 1 parameter"() {
        given: "A database with a collection in context"
            def ctx = new Context()
            def db = new Database("testDB", ctx)
            db.testCollection
            def versioningSuccessful = false

        when: "the database is versioned using 1 argument closure"
            db.eachWithVersionedMap(TransformType.EXPANSION) { dbName ->
                versioningSuccessful = false
            }

        then: "the versioning was unsuccessful"
            thrown(MissingMethodException)
            versioningSuccessful == false
    }

    def "versioning a database rejects a closure with 2 parameters"() {
        given: "A database with a collection in context"
            def ctx = new Context()
            def db = new Database("testDB", ctx)
            db.testCollection
            def versioningSuccessful = false

        when: "the database is versioned using 2 argument closure"
            db.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName ->
                versioningSuccessful = true
            }

        then: "the versioning was rejected"
            thrown(MissingMethodException)
            versioningSuccessful == false
    }

    def "versioning a database accepts a closure with 3 parameters"() {
        given: "A database with a collection in context"
            def ctx = new Context()
            def db = new Database("testDB", ctx)
            db.testCollection
            def versioningSuccessful = false

        when: "the database is versioned with a 3 argument closure"
            db.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName, versionedMap ->
                versioningSuccessful = true
            }

        then: "the versioning was successful"
            notThrown(MissingMethodException)
            versioningSuccessful == true
    }

    def "version multiple collections in EXPANSION mode"() {
        given: "A database with couple of collections in context"
            def ctx = new Context()
            def db = new Database("testDB", ctx)
            def collection1 = db.collection1
            collection1.add("{\"field\" : \"value\"}")
            def collection2 = db.collection2
            collection2.remove("[\"field1\", \"field2\"]")

            def versionedCollections = []
            def versionedMaps = [:]

        when: "each versioned map is invoked on the database"
            db.eachWithVersionedMap(TransformType.EXPANSION) {
                String dbName, String collectionName, versionedMap ->
                    versionedCollections.add(collectionName)
                    versionedMaps[collectionName] = versionedMap
            }

        then: "it versions the corresponding collections as well"
            versionedCollections.equals(["collection1", "collection2"])
            versionedMaps.get("collection1").equals(collection1.asVersionedMap(TransformType.EXPANSION))
            versionedMaps.get("collection2").equals(collection2.asVersionedMap(TransformType.EXPANSION))
    }

    def "version multiple collections in CONTRACTION mode"() {
        given: "A database with couple of collections in context"
            def ctx = new Context()
            def db = new Database("testDB", ctx)
            def collection1 = db.collection1
            collection1.add("{\"field\" : \"value\"}")
            def collection2 = db.collection2
            collection2.remove("[\"field1\", \"field2\"]")

            def versionedCollections = []
            def versionedMaps = [:]

        when: "each versioned map is invoked on the database"
            db.eachWithVersionedMap(TransformType.CONTRACTION) {
                String dbName, String collectionName, versionedMap ->
                    versionedCollections.add(collectionName)
                    versionedMaps[collectionName] = versionedMap
            }

        then: "it versions the corresponding collections as well"
            versionedCollections.equals(["collection1", "collection2"])
            versionedMaps.get("collection1").equals(collection1.asVersionedMap(TransformType.CONTRACTION))
            versionedMaps.get("collection2").equals(collection2.asVersionedMap(TransformType.CONTRACTION))
    }

    def "it throws an exception for invalid operations"() {
        given: "A Database with context"
            def ctx = new Context()
            Database database = new Database("testDB", ctx)

        when: "a collection property's invalid method is invoked"
            database.testCollection.someOperation('{"age":0}')

        then: "Invalid Verb exception is thrown"
            thrown(InvalidGrammar)
    }

    def "it throws an exception for invalid arguments"() {
        given: "A Database with context"
            def ctx = new Context()
            Database database = new Database("testDB", ctx)

        when: "a collection property's add method is invoked with one non-json string argument"
            database.testCollection.add("some field name")

        then: "Invalid Verb exception is thrown"
            thrown(InvalidGrammar)
    }

    def "it throws an exception for operations with no arguments"() {
        given: "A Database with context"
            def ctx = new Context()
            Database database = new Database("testDB", ctx)

        when: "a collection property's method is invoked with no argument"
            database.testCollection.add()

        then: "Invalid Verb exception is thrown"
            thrown(InvalidGrammar)
    }

}
