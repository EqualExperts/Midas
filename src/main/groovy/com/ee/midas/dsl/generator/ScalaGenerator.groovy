package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Databases
import static com.ee.midas.dsl.interpreter.representation.Transform.*

public class ScalaGenerator implements Generator {

    def expansionSnippets = [:]
    def contractionSnippets = [:]


    public ScalaGenerator() {
    }

    @Override
    public String generate(Databases databases) {
        println("Generating Scala Code Midas-Snippets for each transformation...")
        databases.each(EXPANSION) { db, collection, version, operation, args ->
            println("Database and Collection Name = $db.$collection.$version.$operation.$args")
            def codeSnippet = "$operation"(args[0])
            addExpansionSnippet("$db.$collection", version, codeSnippet)
        }
        databases.each(CONTRACTION) { db, collection, version, operation, args ->
            println("Database and Collection Name = $db.$collection.$version.$operation.$args")
            def codeSnippet = "$operation"(args[0])
            addContractionSnippet("$db.$collection", version, codeSnippet)
        }
    }

    private def addExpansionSnippet(String fqName, Long version, String snippet) {
        def collection = expansionSnippets[fqName]
        if(!collection) {
            expansionSnippets[fqName] = [:]
        }
        expansionSnippets[version] = snippet
    }

    private def addContractionSnippet(String fqName, Long version, String snippet) {
        def collection = contractionSnippets[fqName]
        if(!collection) {
            contractionSnippets[fqName] = [:]
        }
        contractionSnippets[version] = snippet
    }

    private String remove(jsonString) {
        """
        def remove1 = (document: BSONObject) => {
            val json = \"""$jsonString \"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document - fields
        }
        """
    }

    private String add(jsonString) {
        """
        (document: BSONObject) => {
            val json = \"""$jsonString \"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document + fields
        }
        """
    }

}
