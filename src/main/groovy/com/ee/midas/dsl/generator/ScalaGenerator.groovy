package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Transform
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.dsl.interpreter.representation.Transform.*

public class ScalaGenerator implements Generator {

    public ScalaGenerator() {
    }

     private def toExpressionName(dbName, collectionName, version, operationName) {
        "${dbName}_${collectionName}_${version}_${operationName}"
     }

    @Override
    public String generate(Tree tree) {
        def snippets = []
        println("Generating Scala Code Midas-Snippets for each transformation...")
        snippets << generateSnippets(EXPANSION, tree)
        snippets << generateSnippets(CONTRACTION, tree)
        snippets.flatten().join('\n')
    }

    private def generateSnippets(Transform transform, Tree tree) {
        def snippets = []
        tree.each(transform) { dbName, collectionName, version, operationName, args ->
            println("Database and Collection Name = $dbName.$collectionName.$version.$operationName.$args")
            def exprName = toExpressionName(dbName, collectionName, version, operationName)
            def snippet = "$operationName"(exprName, args[0])
            snippets << snippet
        }
        snippets
    }

    private String remove(expressionName, jsonString) {
        """
        val ${expressionName} = (document: BSONObject) => {
            val json = \"""$jsonString\"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document - fields
        }
        """
    }

    private String add(expressionName, jsonString) {
        """
        val ${expressionName} = (document: BSONObject) => {
            val json = \"""$jsonString\"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document + fields
        }
        """
    }

}
