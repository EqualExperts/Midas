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
        def snippets = generateResponseSnippets(transformType, tree)

        def responseTransformationEntries = snippets.collect { fullCollectionName, versionedSnippets ->
            """\"$fullCollectionName\" ->
                TreeMap(${versionedSnippets.join("$TAB$TAB, ")})"""
        }.join(', ')

        def requestTransformationEntries =
            generateRequestTransformations(transformType, tree).join(", ")

        if (transformType == EXPANSION) {
            return  """
                    override implicit var transformType = TransformType.EXPANSION

                    override var responseExpansions: Map[String, VersionedSnippets] =
                    Map(${responseTransformationEntries})

                    override var responseContractions: Map[String, VersionedSnippets] =
                    Map()

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map(${requestTransformationEntries})

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map()
                    """
        }
        if (transformType == CONTRACTION) {

            return  """
                    override implicit var transformType = TransformType.CONTRACTION

                    override var responseExpansions: Map[String, VersionedSnippets] =
                    Map()

                    override var responseContractions: Map[String, VersionedSnippets] =
                    Map(${responseTransformationEntries})

                    override var requestExpansions: Map[ChangeSetCollectionKey, Double] =
                    Map()

                    override var requestContractions: Map[ChangeSetCollectionKey, Double] =
                    Map(${requestTransformationEntries})
                    """
        }
        ""
    }

    private def toFullCollectionName(String dbName, String collectionName) {
        "$dbName.$collectionName"
    }

    private def generateResponseSnippets(TransformType transformType, Tree tree) {
        log.info("Started snippets generation for $transformType TransformType...")
        def snippets = [:]
        tree.eachWithVersionedMap(transformType) { String dbName, String collectionName, versionedMap ->
            def versionedSnippets = versionedMap.collect { Long version, Tuple values ->
                def (verb, args, changeSet) = values
                log.info("Generating Snippet for... $dbName.$collectionName [in ChangeSet $changeSet] => Version = $version, Verb = $verb, Args = $args")
                String snippet = "$verb"(*args)
                asScalaResponseMapEntry(version, snippet)
            }
            def fullCollectionName = toFullCollectionName(dbName, collectionName)
            if(!versionedSnippets.isEmpty())
                snippets[fullCollectionName] = versionedSnippets
        }
        log.info("Completed snippets generation for $transformType TransformType!")
        snippets
    }

    private String asScalaResponseMapEntry(Long version, String snippet) {
        "${version}d -> $snippet"
    }

    private def generateRequestTransformations(TransformType transformType, Tree tree) {
        log.info("Started Generating Response Transforms for $transformType TransformType...")
        def response = []
        tree.eachWithVersionedMap(transformType) { String dbName, String collectionName, Map versionedMap ->
            def fullCollectionName = toFullCollectionName(dbName, collectionName)
            response << versionedMap.collectEntries { Long version, Tuple values ->
                def (verb, args, changeSet) = values
                [changeSet, version]
            }.collect { changeSet, version ->
                asScalaRequestMapEntry(changeSet, fullCollectionName, version)
            }
        }
        log.info("Completed Generating Response Transforms $response for $transformType TransformType!")
        response.flatten()
    }

    private String asScalaRequestMapEntry(Long changeSet, String fullCollectionName, Long version) {
        "(${changeSet}L, \"$fullCollectionName\") -> ${version}d"
    }

    //------------------- verb translations ------------------------------
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
