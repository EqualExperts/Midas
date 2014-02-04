package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.transform.TransformType
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

        Verb verb = asVerb(name)
        def parameters = args? args as List<String> : []
        verb.validate(parameters)
        if (verb.isExpansion()) {
            log.info("${this.name} Adding Expansion $verb with $args")
            versionedExpansions[curExpansionVersion++] = [verb, args]
            return
        }
        if (verb.isContraction()) {
            log.info("${this.name} Adding Contraction $verb with $args")
            versionedContractions[curContractionVersion++] = [verb, args]
            return
        }

    }

    @CompileStatic
    private Verb asVerb(String token) {
        try {
            Verb.valueOf(token)
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
            Verb grammar = Verb.valueOf(grammarWithArgs[0] as String)
            def args = grammarWithArgs[1]
            def operation = ['name': grammar.name(), 'args': args]
            ["$entry.key" : operation]
        }
    }

    def String toString() {
        "${getClass().simpleName}: $name, Expansions $versionedExpansions, Contractions $versionedContractions"
    }

}
