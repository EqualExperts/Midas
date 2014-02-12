package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.Parser
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.transform.TransformType.*
import spock.lang.Specification


class ScalaGeneratorSpecs extends Specification {

    def "Generates Scala code for Add operation"() {
        given: 'A Parser builds a Tree for a delta in a change set'
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet) { ->
                using someDatabase
                db.collectionName.add('{"newField" : "newValue"}')
            }
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then: 'generates request and response maps for add operation'
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

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map((1L, \"someDatabase.collectionName\") -> 1d)

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map()
                """
            result.replaceAll(' ', '') == expansionSnippets.replaceAll(' ', '')
   }

    def "Generates Scala code for Remove operation"() {
        given: 'A Parser builds a Tree for a remove delta in a change set'
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet) { ->
                using someDatabase
                db.collectionName.remove('["newField"]')
            }
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(CONTRACTION, tree)

        then: 'generates request and response maps for remove'
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

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map()

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map((1L, "someDatabase.collectionName") -> 1d)
                """
            result.replaceAll(' ', '') == contractionSnippets.replaceAll(' ', '')
    }

    def "Generates Scala code for Copy operation"() {
            given: 'A Parser builds a Tree for a copy delta in a change set'
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet) { ->
                using someDatabase
                db.collectionName.copy("fromOldField", "toNewField")
            }
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then: 'it generates request and response maps for copy operation'
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

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map((1L, "someDatabase.collectionName") -> 1d)

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedCopySnippets.replaceAll(' ', '')
    }

    def "Generates Scala code for Split operation"() {

        given: "a delta for split operation with regex that produces 2 tokens"
            def splitDelta = {
                using someDatabase
                db.collectionName.split("sourceField", "some regex", "{ \"token1\": \"\$1\", \"token2\": \"\$2\"}")
            }

        and: "A Parser builds a Tree for that delta in a changeSet"
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet, splitDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then : 'it generates request and response maps for split operation'
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

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map((1L, "someDatabase.collectionName") -> 1d)

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
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

        and: "A Parser builds a Tree for that delta in a change set"
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet, mergeDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code"
            def result = generator.generate(EXPANSION, tree)

        then: 'it generates request and response maps for merge operation'
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

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map((1L, "someDatabase.collectionName") -> 1d)

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedMergeIntoSnippets.replaceAll(' ', '')
    }

    def "Generates empty Scala maps for expansion delta in contraction mode"() {
        given: "An expansion delta"
            def expansionDelta = {
                using someDatabase
                db.collectionName.add("{\"field1\": \"value1\",\"field2\": \"value2\"}")
            }

        and: "A Parser builds a Tree for expansion delta in a change set"
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet, expansionDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code in contraction mode"
            def result = generator.generate(CONTRACTION, tree)

        then: 'it generates empty request and reponse maps for expansion operation in contraction mode'
        def expectedSnippets =
            """
                override implicit var transformType = TransformType.CONTRACTION

                override var expansions: Map[String, VersionedSnippets] =
                Map()

                override var contractions: Map[String, VersionedSnippets] =
                Map()

                override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                Map()

                override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                Map()
            """
        result.replaceAll(' ', '') == expectedSnippets.replaceAll(' ', '')
    }

    def "Generates empty Scala maps for contraction delta in expansion mode"() {
        given: "A contraction delta"
            def contractionDelta = {
                using someDatabase
                db.collectionName.remove("[\"field1\",\"field2\"]")
            }

        and: "A Parser builds a Tree for contraction delta in a change set"
            Parser parser = new Parser()
            def changeSet = 1
            parser.parse(changeSet, contractionDelta)
            Tree tree = parser.ast()

        and: "A Scala generator"
            ScalaGenerator generator = new ScalaGenerator()

        when: "generator generates scala code in contraction mode"
            def result = generator.generate(EXPANSION, tree)

        then: 'it generates Scala empty request and response maps for contraction operation in expansion mode'
            def expectedSnippets =
                """
                    override implicit var transformType = TransformType.EXPANSION

                    override var expansions: Map[String, VersionedSnippets] =
                    Map()

                    override var contractions: Map[String, VersionedSnippets] =
                    Map()

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map()

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map()
                """
            result.replaceAll(' ', '') == expectedSnippets.replaceAll(' ', '')
    }
}
