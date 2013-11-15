package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Transform
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.dsl.interpreter.representation.Transform.*

public class ScalaGenerator implements Generator {

    private static final String NEW_LINE = '\n'

    private StringBuilder code = new StringBuilder()

    public ScalaGenerator() {
    }

     private def toExpressionName(dbName, collectionName, version, operationName) {
        "${dbName}_${collectionName}_${version}_${operationName}"
     }

    @Override
    public String generate(Tree tree) {
        println('Generating Scala Code Midas-Snippets for each transformation...')
        produce {
            code << 'import com.mongodb.util.JSON' << NEW_LINE
            code << 'import org.bson.BSONObject' << NEW_LINE
            code << 'import com.ee.midas.dsl.Transforms' << NEW_LINE
            code << 'import com.ee.midas.dsl.DocumentOperations._' << NEW_LINE

            code << NEW_LINE
            code << '//WARNING: THIS IS A GENERATED FILE, DO NOT EDIT BY HAND!!' << NEW_LINE
            code << NEW_LINE

            "object Transformations extends Transforms " {
                def expansionSnippets = generateSnippets(EXPANSION, tree)
                code << "${expansionSnippets.values().join(NEW_LINE)}"
                code << NEW_LINE
                code << "override val expansions = ${expansionSnippets.keySet().join(' :: ')} :: Nil"
                code << NEW_LINE
                def contractionSnippets = generateSnippets(CONTRACTION, tree)
                code << "${contractionSnippets.values().join(NEW_LINE)}"
                code << NEW_LINE
                code << "override val contractions = ${contractionSnippets.keySet().join(' :: ')} :: Nil"
                code << NEW_LINE
            }
        }
    }

    def getProperty(String name) {
       println("ScalaGenerator: getProperty Called for $name")
        switch(name) {
            case 'code': return code
            case 'NEW_LINE': return NEW_LINE
            default: return name
        }
    }

    def invokeMethod(String name, Object args) {
        println("ScalaGenerator: invokeMethod Called for $name with $args")
        if(args.length == 1 && args[0] instanceof Closure) {
            code << name << '{' << NEW_LINE
            args[0].delegate = this
            args[0]() << NEW_LINE
            code << '}'
        }
    }

    private String produce(closure) {
        closure.delegate = this
        def result = closure()
        result.toString()
    }

    private def generateSnippets(Transform transform, Tree tree) {
        println("Started snippets generation for $transform Transform...")
        def snippets = [:]
        tree.each(transform) { dbName, collectionName, version, operationName, args ->
            println("Database and Collection Name = $dbName.$collectionName.$version.$operationName.$args")
            def exprName = toExpressionName(dbName, collectionName, version, operationName)
            def snippet = "$operationName"(exprName, args[0])
            snippets[exprName] = snippet
        }
        println("Completed snippets generation for $transform Transform!")
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
