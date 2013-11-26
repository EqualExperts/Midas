package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.transform.TransformType
import spock.lang.Specification

class DatabaseSpecs extends Specification {

   def "adds a collection to a database tree"() {
        given : "A Database"
        Database database = new Database("testDB")
        LinkedHashMap expectedExpansionMap = [1l:[Grammar.add, ['{"age":0}']]]
        LinkedHashMap expectedContractionMap  = [:]

        when : "added a field to a collection"
        database.testCollection.add('{"age":0}')
        Collection collection = database.@collections.get("testCollection")

        then : "collection is added to database tree"
        collection.name.compareTo("testCollection") == 0
        (collection.@versionedExpansions).equals(expectedExpansionMap)
        (collection.@versionedContractions).equals(expectedContractionMap)
   }

    def "Versioned Map for each collection"() {
        given : "A Database"
        Database database = new Database("testDB")
        Integer noOfCollectionsExecuted = 0

        when : "each versioned map is called"
        database.testCollection.add('{"age":0}')
        database.newCollection.add('{"age":0}')
        database.eachWithVersionedMap(TransformType.EXPANSION){ String dbName, String collectionName, versionedMap ->
            ['name':Grammar.add.name(), 'args':['{"age":0}']]
            noOfCollectionsExecuted  += 1 }

        then : "versioned map for each collection is generated"
        noOfCollectionsExecuted == 2
    }



}
