package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.Parser
import com.ee.midas.dsl.interpreter.representation.Tree
import spock.lang.Specification


class ScalaGeneratorSpecs extends Specification {

   def "Generate Snippets"() {
       given : "A Scala generator"
       ScalaGenerator generator = new ScalaGenerator()

       Parser parser  = new Parser()
       parser.parse { ->
           using someDatabase
           db.collectionName.add('{"newField" : "newValue"}')
       }
       Tree tree = parser.ast()
       def expectedSnippets = "override lazy val expansions: Map[String, VersionedSnippets] =\n" +
               "            Map(\"someDatabase.collectionName\" ->\n" +
               "                Map(1d -> \n" +
               "            ((document: BSONObject) => {\n" +
               "                val json = \"\"\"{\"newField\" : \"newValue\"}\"\"\"\n" +
               "                val fields = JSON.parse(json).asInstanceOf[BSONObject]\n" +
               "                document ++ fields\n" +
               "            })\n" +
               "        ))\n" +
               "\n" +
               "        override lazy val contractions: Map[String, VersionedSnippets] =\n" +
               "            Map(\"someDatabase.collectionName\" ->\n" +
               "                Map())"

       when : "passed a tree"
       def result = generator.generate(tree)

       then : "generates valid Scala snippets"
       result.compareTo(expectedSnippets)
   }
}
