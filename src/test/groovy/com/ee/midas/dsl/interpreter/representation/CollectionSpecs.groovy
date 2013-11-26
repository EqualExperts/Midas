package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.transform.TransformType
import spock.lang.Specification

class CollectionSpecs extends Specification {

   def "Versioned map for single EXPANSION"() {
       given : "A Collection"
       Collection collection = new Collection("testCollection")
       LinkedHashMap expectedMap  = [1:['name':Grammar.add.name(), 'args':['{"age":0}']]]

       when : "it creates a versioned Map for EXPANSION type"
       collection.add('{"age":0}')
       LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

       then : "the map should contain operation of expansion type"
       areMapsEqual(versionedMap,expectedMap)

    }

    def "Versioned map for single CONTRACTION"() {
        given : "A Collection"
        Collection collection = new Collection("testCollection")
        LinkedHashMap expectedMap  = [1:['name':Grammar.remove.name(), 'args':['{"age":0}']]]

        when : "it creates a versioned Map for CONTRACTION type"
        collection.remove('{"age":0}')
        LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then : "the map should contain operation of contraction type"
        areMapsEqual(versionedMap,expectedMap)

    }

    def "Versioned map for multiple EXPANSION"() {
        given : "A Collection"
        Collection collection = new Collection("testCollection")
        LinkedHashMap expectedMap  = [1:['name':Grammar.add.name(), 'args':['{"age":0}']], 2:['name':Grammar.add.name(), 'args':['{"city":"pune"}']]]

        when : "it creates a versioned Map for EXPANSION type"
        collection.add('{"age":0}')
        collection.add('{"city":"pune"}')
        LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.EXPANSION)

        then : "the map should contain operations of expansion type"
        areMapsEqual(versionedMap,expectedMap)
    }

    def "Versioned map for multiple CONTRACTION"() {
        given : "A Collection"
        Collection collection = new Collection("testCollection")
        LinkedHashMap expectedMap  = [1:['name':Grammar.remove.name(), 'args':['{"age":0}']], 2:['name':Grammar.remove.name(), 'args':['{"city":"pune"}']]]

        when : "it creates a versioned Map for CONTRACTION type"
        collection.remove('{"age":0}')
        collection.remove('{"city":"pune"}')
        LinkedHashMap versionedMap = collection.asVersionedMap(TransformType.CONTRACTION)

        then : "the map should contain operations of contraction type"
        areMapsEqual(versionedMap,expectedMap)

    }

    def areMapsEqual(map1, map2) {
        map1.every {k , v ->
            if ( !map2.containsKey(Eval.me(k))){ println("contains key "+k); return false }
            if ( v instanceof Map) {
                if (!v.equals(map2[Eval.me(k)]) ) return false
            }
            else{
                if ( v != map2[Eval.me(k)] ) return false
            }
            true
        }

    }

}


