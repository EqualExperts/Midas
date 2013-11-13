package com.ee.midas.dsl

import com.ee.midas.dsl.generator.ScalaGenerator
import com.ee.midas.dsl.interpreter.Parser

def parser = new Parser()
def representation = parser.parse {
     //User writes these in a separate file
//     using test
//     db.things.add('[]')   //version 1
//     db.things.add('{}')   //version 2

     using transactions
     db.orders.add('{ "field1" : 1, "field2" : 2.0, "field3": true, "field4": null}') //version 1

     using test
     db.things.remove('["field10"]')  //version 3

     using transactions
     db.orders.add('{ "field5": [], "field6": {} }') //version 2

     using ee
     db.people.add('{ "age" : 0 }')  //version 1

//    use 'tester'

 }

def generator = new ScalaGenerator()
println generator.generate(representation)