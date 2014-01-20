package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Contraction
import com.ee.midas.dsl.grammar.Expansion
import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.transform.TransformType
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import static com.ee.midas.transform.TransformType.CONTRACTION
import static com.ee.midas.transform.TransformType.EXPANSION


@ToString
@Slf4j
class Collection {
    final String name
    private final Map<Long, Tuple> versionedExpansions = [:]
    private final Map<Long, Tuple> versionedContractions = [:]
    private Long curExpansionVersion = 1
    private Long curContractionVersion = 1

    Collection(String name) {
        this.name = name
    }

    def invokeMethod(String name, args) {
        log.info("${this.name} invokeMethod: Operation $name with $args")

        if(args) {
            Grammar grammar = asGrammar(name)
            grammar.validate(args as List<String>)
            if (isExpansion(grammar)) {
                log.info("${this.name} Adding Expansion $grammar with $args")
                versionedExpansions[curExpansionVersion++] = [grammar, args]
                return
            }
            if (isContraction(grammar)) {
                log.info("${this.name} Adding Contraction $grammar with $args")
                versionedContractions[curContractionVersion++] = [grammar, args]
                return
            }
        }
    }

    @CompileStatic
    private boolean isExpansion(Grammar grammar) {
        Grammar.class.getDeclaredField(grammar.name()).getAnnotation(Expansion.class) != null
    }

    @CompileStatic
    private boolean isContraction(Grammar grammar) {
        Grammar.class.getDeclaredField(grammar.name()).getAnnotation(Contraction.class) != null
    }

    @CompileStatic
    private Grammar asGrammar(String token) {
        try {
            Grammar.valueOf(token)
        } catch (IllegalArgumentException iae) {
            throw new InvalidGrammar("Sorry!! Midas Compiler doesn't understand $token")
        }
    }

    @CompileStatic
    def asVersionedMap(TransformType transformType) {
        Map<Long, Tuple> versionedTransforms = null
        if(transformType == EXPANSION) {
            versionedTransforms = versionedExpansions
        }

        if(transformType == CONTRACTION) {
            versionedTransforms = versionedContractions
        }

        versionedTransforms.collectEntries { Map.Entry entry ->
            Tuple grammarWithArgs = (Tuple) entry.value
            Grammar grammar = Grammar.valueOf(grammarWithArgs[0] as String)
            def args = grammarWithArgs[1]
            def operation = ['name': grammar.name(), 'args': args]
            ["$entry.key" : operation]
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name, Expansions $versionedExpansions, Contractions $versionedContractions"
    }

}
