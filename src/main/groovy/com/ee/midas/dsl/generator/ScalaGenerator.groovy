package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.transform.TransformType
import groovy.util.logging.Slf4j

import static com.ee.midas.transform.TransformType.CONTRACTION
import static com.ee.midas.transform.TransformType.EXPANSION

@Slf4j
public class ScalaGenerator implements Generator<String> {

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

            override var contractions: Map[String, VersionedSnippets] =
            Map()
            """
        } else if (transformType == CONTRACTION) {
            """
            override var expansions: Map[String, VersionedSnippets] =
            Map()

            override var contractions: Map[String, VersionedSnippets] =
            Map(${transformationEntries})
            """
        } else {
            """
            override var expansions: Map[String, VersionedSnippets] = Map()

            override var contractions: Map[String, VersionedSnippets] = Map()
            """
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
            if(!versionedSnippets.isEmpty())
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
                document ++ (fields, false)
            })
        """.stripMargin()
    }

    private String copy(String fromField, String toField) {
        """
            ((document: BSONObject) => {
                document(\"$fromField\") match {
                    case Some(fromFieldValue) => document(\"$toField\") = fromFieldValue
                    case None => document
                }
            })
        """.stripMargin()
    }

    private String merge(String fieldsArray, String separator, String mergeField) {
        def fields = fieldsArray.substring(1, fieldsArray.length() - 1)
        """
            ((document: BSONObject) => {
                val fields = List($fields)
                document >~< (\"$mergeField\", \"$separator\", fields)
            })
        """.stripMargin()
    }

    //todo: make split more performant by removing Pattern.compile at runtime, use memoization?.
    private String split(String splitField, String regex, String json) {
        """
            ((document: BSONObject) => document <~> (\"$splitField\", Pattern.compile(\"$regex\"), \"""$json\"""))
        """.stripMargin()
    }

    private String transform(String outputField, String expressionJson) {
        """
            ((document: BSONObject) => {
                val expression = parse(\"""$expressionJson\""")
                val literal = expression.evaluate(document)
                document + (\"$outputField\", literal.value)
            })
        """.stripMargin()
    }
}
