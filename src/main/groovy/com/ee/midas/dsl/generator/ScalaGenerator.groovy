package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Transform
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.dsl.interpreter.representation.Transform.*

public class ScalaGenerator implements Generator {

    private static final String NEW_LINE = '\n'
    private static final String TAB = '\t'

//    private StringBuilder code = new StringBuilder()

    public ScalaGenerator() {
    }

     private def toExpressionName(dbName, collectionName, version, operationName) {
        "${dbName}_${collectionName}_${version}_${operationName}"
     }

    @Override
    public String generate(Tree tree) {
        println('Generating Scala Code Midas-Snippets for each transformation...')
        def expansionSnippets = generateSnippets(EXPANSION, tree)
        def contractionSnippets = generateSnippets(CONTRACTION, tree)

        def expansionEntries = expansionSnippets.collect { fullCollectionName, versionedSnippets ->
            """\"$fullCollectionName\" ->
                Map(${versionedSnippets.join("$TAB$TAB, ")})"""
        }.join(', ')


        def contractionEntries = contractionSnippets.collect { fullCollectionName, versionedSnippets ->
            """\"$fullCollectionName\" ->
                Map(${versionedSnippets.join("$TAB$TAB, ")})""".stripMargin()
        }.join(', ')

        """
        override lazy val expansions: Map[String, VersionedSnippets] =
            Map(${expansionEntries})

        override lazy val contractions: Map[String, VersionedSnippets] =
            Map(${contractionEntries})
        """.stripMargin()
    }

    private def toFullCollectionName(String dbName, String collectionName) {
        "$dbName.$collectionName"
    }

    private def generateSnippets(Transform transform, Tree tree) {
        println("Started snippets generation for $transform Transform...")
        def snippets = [:]
        tree.eachWithVersionedMap(transform) { String dbName, String collectionName, versionedMap ->
            def versionedSnippets = versionedMap.collect { version, operation ->
                def operationName = operation['name']
                def args = operation['args']
                println("Database and Collection Name = $dbName.$collectionName.$version.$operationName.$args")
//                def exprName = toExpressionName(dbName, collectionName, version, operationName)
                def snippet = "$operationName"(args[0])
                "$version -> $snippet"
            }
            def fullCollectionName = toFullCollectionName(dbName, collectionName)
            snippets[fullCollectionName] = versionedSnippets
        }
        println("Completed snippets generation for $transform Transform!")
        snippets
    }

    //------------------- operations ------------------------------
    private String remove(jsonString) {
        """
            ((document: BSONObject) => {
                val json = \"""$jsonString\"""
                val fields = JSON.parse(json).asInstanceOf[BSONObject]
                document -- fields
            })
        """.stripMargin()
    }

    private String add(jsonString) {
        """
            ((document: BSONObject) => {
                val json = \"""$jsonString\"""
                val fields = JSON.parse(json).asInstanceOf[BSONObject]
                document ++ fields
            })
        """.stripMargin()
    }

}
