package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.transform.TransformType
import spock.lang.Specification

class TreeSpecs extends Specification {

    def "adds a single database to a tree"() {
        given: "A Tree"
            Tree tree = new Tree()

        when: "a database is used"
            def database = tree.using("testDB")

        then: "databases are added to the tree"
            database instanceof Database
            database.getName().equals("testDB")
    }

    def "does not version a database with no collections"() {
        given: "A tree with a database"
            Tree tree = new Tree()
            tree.using("testDB")
            def versionedDatabase = false

        when: "the tree is versioned"
            tree.eachWithVersionedMap(TransformType.EXPANSION) {
                versionedDatabase = true
            }

        then: "the database was not versioned"
            versionedDatabase == false
    }

    def "adds a collection to the tree"() {
        given: "A tree"
            Tree tree = new Tree()
            def db = tree.using("testDB")

        when: "a collection with database is added to it"
            def collection = db.testCollection

        then: "a collection is added to the tree"
            collection instanceof Collection
            collection.name == "testCollection"
    }

    def "versions a database with collections"() {
        given: "A tree with a database and collection"
            Tree tree = new Tree()
            def db = tree.using("testDB")
            db.testCollection
            def versionedDatabase = false

        when: "the tree is versioned"
            tree.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName, versionedMap ->
                versionedDatabase = true
            }

        then: "the database was successfully versioned"
            versionedDatabase == true
    }

    def "versioning a tree rejects a closure with 1 parameter"() {
        given: "A tree with a database and a collection"
            Tree tree = new Tree()
            def db = tree.using("testDB")
            db.testCollection
            def versioningSuccessful = false

        when: "the tree is versioned with a 1 argument closure"
            tree.eachWithVersionedMap(TransformType.EXPANSION) { dbName ->
                versioningSuccessful = false
            }

        then: "the versioning was rejected"
            thrown(MissingMethodException)
            versioningSuccessful == false
    }

    def "versioning a tree rejects a closure with 2 parameters"() {
        given: "A tree with a database and a collection"
            Tree tree = new Tree()
            def db = tree.using("testDB")
            db.testCollection
            def versioningSuccessful = false

        when: "the tree is versioned with a 2 argument closure"
            tree.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName ->
                versioningSuccessful = true
            }

        then: "the versioning was rejected"
            thrown(MissingMethodException)
            versioningSuccessful == false
    }

    def "versioning a tree accepts a closure with 3 parameters"() {
        given: "A tree with a database and a collection"
            Tree tree = new Tree()
            def db = tree.using("testDB")
            db.testCollection
            def versioningSuccessful = false

        when: "the tree is versioned with a 3 argument closure"
            tree.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName, versionedMap ->
                versioningSuccessful = true
            }

        then: "the versioning was successful"
            notThrown(MissingMethodException)
            versioningSuccessful == true
    }

    def "versioning a tree rejects a closure with more than 3 parameters"() {
        given: "A tree with a database and a collection"
            Tree tree = new Tree()
            def db = tree.using("testDB")
            db.testCollection
            def versioningSuccessful = false

        when: "the tree is versioned"
            tree.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName, versionedMap, extraParam ->
                versioningSuccessful = true
            }

        then: "the versioning was successful"
            thrown(MissingMethodException)
            versioningSuccessful == false
    }

    def "versions the database and its collections with operations"() {
        given: "A tree with a database and collection with expansion operation"
            Tree tree = new Tree()
            def db = tree.using("testDB")
            def collection = db.testCollection
            collection.add("{\"newField\":\"defaultValue\"}")

            def actualVersionedDatabase
            def actualVersionedCollection
            def actualVersionedMap

        when: "the tree is versioned"
            tree.eachWithVersionedMap(TransformType.EXPANSION) { dbName, collectionName, versionedMap ->
                actualVersionedDatabase = dbName
                actualVersionedCollection = collectionName
                actualVersionedMap = versionedMap
            }

        then: "the database and collection was successfully versioned"
            actualVersionedDatabase == "testDB"
            actualVersionedCollection == "testCollection"
            actualVersionedMap.equals(collection.asVersionedMap(TransformType.EXPANSION))
    }

    def "version multiple databases and collections in EXPANSION mode"() {
        given: "A Tree with 2 databases"
            def tree = new Tree()

            def db1 = tree.using("testDB1")
            def db2 = tree.using("testDB2")
            def collection1 = db1.collection1
            def collection2 = db2.collection2
            collection1.add("{\"field\" : \"value\"}")
            collection2.remove("{\"field\" : \"value\"}")

            def versionedDatabases = []
            def versionedCollections = []
            def versionedMaps = [:]

        when: "each versioned map is invoked on the database"
            tree.eachWithVersionedMap(TransformType.EXPANSION) {
                String dbName, String collectionName, versionedMap ->
                    versionedDatabases.add(dbName)
                    versionedCollections.add(collectionName)
                    versionedMaps[collectionName] = versionedMap
            }

        then: "it versions the corresponding databases as well"
            versionedDatabases.equals(["testDB1", "testDB2"])
            versionedCollections.equals(["collection1", "collection2"])
            versionedMaps.get("collection1").equals(collection1.asVersionedMap(TransformType.EXPANSION))
            versionedMaps.get("collection2").equals(collection2.asVersionedMap(TransformType.EXPANSION))
    }

    def "version multiple databases and collections in CONTRACTION mode"() {
        given: "A Tree with 2 databases"
            def tree = new Tree()

            def db1 = tree.using("testDB1")
            def db2 = tree.using("testDB2")
            def collection1 = db1.collection1
            def collection2 = db2.collection2
            collection1.add("{\"field\" : \"value\"}")
            collection2.remove("{\"field\" : \"value\"}")

            def versionedDatabases = []
            def versionedCollections = []
            def versionedMaps = [:]

        when: "each versioned map is invoked on the database"
            tree.eachWithVersionedMap(TransformType.CONTRACTION) {
                String dbName, String collectionName, versionedMap ->
                    versionedDatabases.add(dbName)
                    versionedCollections.add(collectionName)
                    versionedMaps[collectionName] = versionedMap
            }

        then: "it versions the corresponding databases as well"
            versionedDatabases.equals(["testDB1", "testDB2"])
            versionedCollections.equals(["collection1", "collection2"])
            versionedMaps.get("collection1").equals(collection1.asVersionedMap(TransformType.CONTRACTION))
            versionedMaps.get("collection2").equals(collection2.asVersionedMap(TransformType.CONTRACTION))
    }

    def "it throws an exception for invalid operations"() {
        given: "A Tree"
            Tree tree = new Tree()
            def database = tree.using("testDB")

        when: "a collection property's invalid method is invoked"
            database.testCollection.someOperation('{"age":0}')

        then: "Invalid Grammar exception is thrown"
            thrown(InvalidGrammar)
    }

    def "it throws an exception for invalid arguments"() {
        given: "A Tree"
            Tree tree = new Tree()
            def database = tree.using("testDB")

        when: "a collection property's add method is invoked with one non-json string argument"
            database.testCollection.add("some field name")

        then: "Invalid Grammar exception is thrown"
            thrown(InvalidGrammar)
    }

    def "it throws an exception for operations with no arguments"() {
        given: "A Tree"
            Tree tree = new Tree()
            def database = tree.using("testDB")

        when: "a collection property's method is invoked with no arguments"
            database.testCollection.add()

        then: "Invalid Grammar exception is thrown"
            thrown(InvalidGrammar)
    }

}
