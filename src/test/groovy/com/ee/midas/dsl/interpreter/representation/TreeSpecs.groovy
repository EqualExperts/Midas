package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.transform.TransformType
import spock.lang.Specification

class TreeSpecs extends Specification {

    def "adds a database to tree"() {
        given : "A Tree"
        Tree tree = new Tree()
        Database database1 = new Database("testDB")
        Database database2 = new Database("secondDB")
        LinkedHashMap expectedDatabases = ['testDB' :database1 , 'secondDB' : database2]

        when : "added a database to the tree"
        tree.use("testDB")
        tree.use("secondDB")
        def databases = tree.@databases

        then : "databases are added to the tree"
        (databases.keySet()).equals(expectedDatabases.keySet())
        !(databases.get("testDB")).equals(null)
        !(databases.get("secondDB")).equals(null)
    }

    def "Versioned Map for each database"() {
        given : "A Tree "
        Tree tree = new Tree()
        Integer noOfDatabaseExcecuted = 0
        def database1 = tree.use("testDB")
        database1.testCollection.add('{"age":0}')

        def database2 = tree.use("newDB")
        database2.newCollection.add('{"age":0}')

        when : "eachVersionedMap is called"
        tree.eachWithVersionedMap(TransformType.EXPANSION) {
            String dbName, String collectionName, versionedMap ->
            ['name':Grammar.add.name(), 'args':['{"age":0}']]
            noOfDatabaseExcecuted += 1
        }

        then : "VersionedMap is created for each database"
        noOfDatabaseExcecuted == 2
    }
}
