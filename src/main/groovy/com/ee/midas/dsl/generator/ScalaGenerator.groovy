package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.transform.TransformType
import groovy.util.logging.Slf4j

import static com.ee.midas.transform.TransformType.CONTRACTION
import static com.ee.midas.transform.TransformType.EXPANSION

@Slf4j
public class ScalaGenerator implements Generator {

    private static final String TAB = '\t'

    public ScalaGenerator() {
    }

    @Override
    public String generate(TransformType transformType, Tree tree) {
        log.info('Generating Scala Code Midas-Snippets for $transformType...')
        def snippets = generateSnippets(transformType, tree)

        def transformationEntries = snippets.collect { fullCollectionName, versionedSnippets ->
            """\"$fullCollectionName\" ->
                Map(${versionedSnippets.join("$TAB$TAB, ")})"""
        }.join(', ')

        if (transformType == EXPANSION) {
            """
            override var expansions: Map[String, VersionedSnippets] =
            Map(${transformationEntries})
            """
        } else if (transformType == CONTRACTION) {
            """
            override var contractions: Map[String, VersionedSnippets] =
            Map(${transformationEntries})
            """
        } else {
            ''
        }
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
                def args = operation['args'] as List<String>
                log.info("Generating Snippet for... $dbName.$collectionName => Version = $version, Operation = $operationName, Args = $args")
                def snippet = "$operationName"(*args)
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

    private String copy(fromFieldString, toFieldString) {
        """
            ((document: BSONObject) => {
                val fromFieldValue = document.get(\"$fromFieldString\")
                document + (\"$toFieldString\", fromFieldValue)
            })
        """.stripMargin()
    }

}
