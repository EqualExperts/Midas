package com.ee.midas.dsl.interpreter.representation


import com.ee.midas.dsl.grammar.*
import groovy.transform.Immutable

import static com.ee.midas.dsl.interpreter.representation.Transform.*
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.ToString


@ToString
class Collection {
    final String name
    private final jsonSlurper = new JsonSlurper()
    private final def versionedExpansions = [:]
    private final def versionedContractions = [:]
    private Long curExpansionVersion = 1
    private Long curContractionVersion = 1

    Collection(String name) {
        this.name = name
    }

    def invokeMethod(String name, args) {
        println("Collection: ${this.name} invokeMethod: Operation $name with $args")

        if(args) {
            Grammar grammar = validateGrammar(name)

            def jsonString = args[0].toString()
            validateJson(jsonString)

            if (isExpansion(grammar)) {
                println("Collection: ${this.name} Adding Expansion $grammar with $args")
                versionedExpansions[curExpansionVersion++] = [grammar, args]
                return
            }
            if (isContraction(grammar)) {
                println("Collection: ${this.name} Adding Contraction $grammar with $args")
                versionedContractions[curContractionVersion++] = [grammar, args]
                return
            }
        }
    }

    private boolean isExpansion(Grammar grammar) {
        Grammar.class.getDeclaredField(grammar.name()).getAnnotation(Expansion.class) != null
    }

    private boolean isContraction(Grammar grammar) {
        Grammar.class.getDeclaredField(grammar.name()).getAnnotation(Contraction.class) != null
    }

    private Grammar validateGrammar(String token) {
        try {
            Grammar.valueOf(token)
        } catch (IllegalArgumentException iae) {
            throw new InvalidGrammar("Sorry!! Midas Compiler doesn't understand $token")
        }
    }

    private void validateJson(jsonString) {
        println("Parsing Input JSON...$jsonString")
        try {
            jsonSlurper.parseText(jsonString)
        } catch (JsonException je) {
            throw new InvalidGrammar("MidasCompiler: Error: $je.message")
        }
        println("Parsed Input JSON Successfully")
    }


    def each(Transform transform, String dbName, closure) {
        def versionedTransform = null
        if(transform == EXPANSION) {
            versionedTransform = versionedExpansions
        }

        if(transform == CONTRACTION) {
            versionedTransform = versionedContractions
        }

        versionedTransform.each { version, grammarWithArgs ->
            def (grammar, args) = grammarWithArgs
            closure(dbName, name, version, grammar.name(), args)
        }
    }

    def asVersionedMap(Transform transform) {
        def versionedTransform = null
        if(transform == EXPANSION) {
            versionedTransform = versionedExpansions
        }

        if(transform == CONTRACTION) {
            versionedTransform = versionedContractions
        }

        versionedTransform.collectEntries { entry ->
            def grammarWithArgs = entry.value
            def (grammar, args) = grammarWithArgs
            def operation = ['name': grammar.name(), 'args': args]
            ["$entry.key" : operation]
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name, Expansions $versionedExpansions, Contractions $versionedContractions"
    }

}
