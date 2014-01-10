package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.Parser
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.transform.TransformType.*
import spock.lang.Specification


class ScalaGeneratorSpecs extends Specification {

   def "Generate Snippets for Expansion"() {
       given : "A Parser builds a Tree"
           Parser parser = new Parser()
           parser.parse { ->
               using someDatabase
               db.collectionName.add('{"newField" : "newValue"}')
           }
           Tree tree = parser.ast()

       and : "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

       when : "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

       then : "generates Scala snippets"
           def expansionSnippets =
              """
              override var expansions: Map[String, VersionedSnippets] =
              Map(\"someDatabase.collectionName\" ->
                                  Map(1d ->
                              ((document: BSONObject) => {
                                  val json = \"\"\"{\"newField\" : \"newValue\"}\"\"\"
                                  val fields = JSON.parse(json).asInstanceOf[BSONObject]
                                 document ++ fields
                           })
                       ))
              """
           result.replaceAll(' ', '') == expansionSnippets.replaceAll(' ', '')
   }

   def "Generate Snippets for Contraction"() {
       given : "A Parser builds a Tree"
           Parser parser  = new Parser()
           parser.parse { ->
               using someDatabase
               db.collectionName.remove('["newField"]')
           }
           Tree tree = parser.ast()

       and : "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

       when : "generator generates scala code"
            def result = generator.generate(CONTRACTION, tree)

       then : "generates Scala snippets"
           def contractionSnippets =
                   """
                   override var contractions: Map[String, VersionedSnippets] =
                   Map(\"someDatabase.collectionName\" ->
                          Map(1d ->
                      ((document: BSONObject) => {
                          val json = \"\"\"[\"newField\"]\"\"\"
                          val fields = JSON.parse(json).asInstanceOf[BSONObject]
                          document -- fields
                      })
                   ))
                   """
            result.replaceAll(' ', '') == contractionSnippets.replaceAll(' ', '')
   }
}
