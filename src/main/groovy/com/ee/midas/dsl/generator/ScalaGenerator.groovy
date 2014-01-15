package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.transform.TransformType
import groovy.json.JsonSlurper
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
                TreeMap(${versionedSnippets.join("$TAB$TAB, ")})"""
        }.join(', ')

        if (transformType == EXPANSION) {
            """
            override var expansions: Map[String, VersionedSnippets] =
            Map(${transformationEntries})

            override var contractions: Map[String, VersionedSnippets] = Map()
            """
        } else if (transformType == CONTRACTION) {
            """
            override var expansions: Map[String, VersionedSnippets] = Map()

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
    private String remove(String json) {
        """
            ((document: BSONObject) => {
                val json = \"""$json\"""
                val fields = JSON.parse(json).asInstanceOf[BSONObject]
                document -- fields
            })
        """.stripMargin()
    }

    private String add(String json) {
        """
            ((document: BSONObject) => {
                val json = \"""$json\"""
                val fields = JSON.parse(json).asInstanceOf[BSONObject]
                document ++ fields
            })
        """.stripMargin()
    }

    private String copy(String fromField, String toField) {
        """
            ((document: BSONObject) => {
                document(\"$toField\") = document(\"$fromField\")
            })
        """.stripMargin()
    }

    private String mergeInto(String mergeField, String separator, String fieldsArray) {
        def fields = fieldsArray.substring(1, fieldsArray.length() - 1)
        """
            ((document: BSONObject) => {
                val fields = List($fields)
                document >~< (\"$mergeField\", \"$separator\", fields)
            })
        """.stripMargin()
    }

    private String split(String splitField, String regex, String json) {
        """
            ((document: BSONObject) => document <~> (\"$splitField\", Pattern.compile(\"$regex\"), \"""$json\"""))
        """.stripMargin()
    }
}
