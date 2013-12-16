package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.transform.TransformType
import groovy.util.logging.Slf4j

import static com.ee.midas.transform.TransformType.CONTRACTION
import static com.ee.midas.transform.TransformType.EXPANSION

@Slf4j
public class ScalaGenerator implements Generator {

    private static final String NEW_LINE = '\n'
    private static final String TAB = '\t'

    public ScalaGenerator() {
    }

    @Override
    public String generate(Tree tree) {
        log.info('Generating Scala Code Midas-Snippets for each transformation...')
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
        override var expansions: Map[String, VersionedSnippets] =
            Map(${expansionEntries})

        override var contractions: Map[String, VersionedSnippets] =
            Map(${contractionEntries})
        """.stripMargin()
    }

    private def toFullCollectionName(String dbName, String collectionName) {
        "$dbName.$collectionName"
    }

    private def generateSnippets(TransformType transformType, Tree tree) {
        log.info("Started snippets generation for $transformType TransformType...")
        def snippets = [:]
        tree.eachWithVersionedMap(transformType) { String dbName, String collectionName, versionedMap ->
            def versionedSnippets = versionedMap.collect { version, operation ->
                def operationName = operation['name']
                def args = operation['args']
                log.info("Generating Snippet for... $dbName.$collectionName => Version = $version, Operation = $operationName, Args = $args")
                def snippet = "$operationName"(args[0])
                "${version}d -> $snippet"
            }
            def fullCollectionName = toFullCollectionName(dbName, collectionName)
            snippets[fullCollectionName] = versionedSnippets
        }
        log.info("Completed snippets generation for $transformType TransformType!")
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
