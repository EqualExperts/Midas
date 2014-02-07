package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.Parser
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.transform.TransformType.*
import spock.lang.Specification


class ScalaGeneratorSpecs extends Specification {

    def "Generate Snippets for Add operation"() {
        given: "A Parser builds a Tree"
            Parser parser = new Parser()
            parser.parse { ->
                using someDatabase
                db.collectionName.add('{"newField" : "newValue"}')
            }
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then: "generates Scala snippets"
            def expansionSnippets =
                """
                    override implicit var transformType = TransformType.EXPANSION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map(\"someDatabase.collectionName\" ->
                        TreeMap(1d ->
                            ((document: BSONObject) => {
                              val json = \"\"\"{\"newField\" : \"newValue\"}\"\"\"
                              val fields = JSON.parse(json).asInstanceOf[BSONObject]
                              document ++ (fields, false)
                            })
                    ))

                    override var contractions: Map[String, VersionedSnippets] =
                    Map()
                """
            result.replaceAll(' ', '') == expansionSnippets.replaceAll(' ', '')
   }

    def "Generate Snippets for Remove operation"() {
        given: "A Parser builds a Tree"
            Parser parser  = new Parser()
            parser.parse { ->
                using someDatabase
                db.collectionName.remove('["newField"]')
            }
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(CONTRACTION, tree)

        then: "generates Scala snippets"
            def contractionSnippets =
                """
                    override implicit var transformType = TransformType.CONTRACTION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map()

                    override var contractions: Map[String, VersionedSnippets] =
                    Map(\"someDatabase.collectionName\" ->
                        TreeMap(1d ->
                            ((document: BSONObject) => {
                              val json = \"\"\"[\"newField\"]\"\"\"
                              val fields = JSON.parse(json).asInstanceOf[BSONObject]
                              document -- fields
                            })
                    ))
                """
            result.replaceAll(' ', '') == contractionSnippets.replaceAll(' ', '')
    }

    def "Generate Snippets for Copy operation"() {
        given: "A Parser builds a Tree for a copy operation"
            Parser parser  = new Parser()
            parser.parse { ->
                using someDatabase
                db.collectionName.copy("fromOldField", "toNewField")
            }
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then: "it generates Scala snippets for copy operation"
            def expectedCopySnippets =
                """
                    override implicit var transformType = TransformType.EXPANSION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map(\"someDatabase.collectionName\" ->
                        TreeMap(1d ->
                            ((document: BSONObject) => {
                              document(\"fromOldField\") match {
                                case Some(fromFieldValue) => document(\"toNewField\") = fromFieldValue
                                case None => document
                              }
                            })
                    ))

                    override var contractions: Map[String, VersionedSnippets] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedCopySnippets.replaceAll(' ', '')
    }

    def "Generate Snippets for Split operation"() {

        given: "a delta for split operation with regex that produces 2 tokens"
            def splitDelta = {
                using someDatabase
                db.collectionName.split("sourceField", "some regex", "{ \"token1\": \"\$1\", \"token2\": \"\$2\"}")
            }

        and: "A Parser builds a Tree for a split operation"
            Parser parser  = new Parser()
            parser.parse(splitDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then : "it generates Scala snippets for split operation"
            def expectedSplitSnippets =
                """
                    override implicit var transformType = TransformType.EXPANSION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map(\"someDatabase.collectionName\" ->
                        TreeMap(1d ->
                            ((document: BSONObject) => document <~> ("sourceField", Pattern.compile("someregex"), \"""{"token1": "\$1", "token2": "\$2" }""\"))
                    ))

                    override var contractions: Map[String, VersionedSnippets] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedSplitSnippets.replaceAll(' ', '')
    }

    def "Generate Snippets for MergeInto operation"() {

        given: "a delta for merge operation with separator"
            def mergeDelta = {
                using someDatabase
                db.collectionName.merge("[\"field1\",\"field2\"]", "separator", "targetField")
            }

        and: "A Parser builds a Tree for a merge operation"
            Parser parser = new Parser()
            parser.parse(mergeDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then: "it generates Scala snippets for merge operation"
            def expectedMergeIntoSnippets =
                """
                    override implicit var transformType = TransformType.EXPANSION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map(\"someDatabase.collectionName\" ->
                        TreeMap(1d ->
                            ((document: BSONObject) => {
                              val fields = List("field1","field2")
                              document >~< ("targetField", "separator", fields)
                            })
                    ))

                    override var contractions: Map[String, VersionedSnippets] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedMergeIntoSnippets.replaceAll(' ', '')
    }

    def "Generates empty maps for expansion delta in contraction mode"() {
        given: "An expansion delta"
            def expansionDelta = {
                using someDatabase
                db.collectionName.add("{\"field1\": \"value1\",\"field2\": \"value2\"}")
            }

        and: "A Parser builds a Tree for expansion operation"
            Parser parser = new Parser()
            parser.parse(expansionDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code in contraction mode"
            def result = generator.generate(CONTRACTION, tree)

        then: "it generates Scala snippet of empty maps for expansion operation in contraction mode"
        def expectedSnippets =
            """
                override implicit var transformType = TransformType.CONTRACTION

                override var expansions: Map[String, VersionedSnippets] =
                Map()

                override var contractions: Map[String, VersionedSnippets] =
                Map()
            """
        result.replaceAll(' ', '') == expectedSnippets.replaceAll(' ', '')
    }

    def "Generates empty maps for contraction delta in expansion mode"() {
        given: "A contraction delta"
            def contractionDelta = {
                using someDatabase
                db.collectionName.remove("[\"field1\",\"field2\"]")
            }

        and: "A Parser builds a Tree for contraction operation"
            Parser parser = new Parser()
            parser.parse(contractionDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code in contraction mode"
            def result = generator.generate(EXPANSION, tree)

        then: "it generates Scala snippet of empty maps for contraction operation in expansion mode"
            def expectedSnippets =
                """
                    override implicit var transformType = TransformType.EXPANSION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map()

                    override var contractions: Map[String, VersionedSnippets] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedSnippets.replaceAll(' ', '')
    }
}
