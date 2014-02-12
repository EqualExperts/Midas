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
    private final Map<Long, Tuple> versionedExpansions = [:] as LinkedHashMap
    private final Map<Long, Tuple> versionedContractions = [:] as LinkedHashMap
    private Long curExpansionVersion = 1
    private Long curContractionVersion = 1
    private final Context ctx

    Collection(String name, Context ctx) {
        this.name = name
        this.ctx = ctx
    }

    def invokeMethod(String name, args) {
        log.info("${this.name} invokeMethod: Operation $name with $args")

        Verb verb = asVerb(name)
        def parameters = args? args as List<String> : []
        verb.validate(parameters)
        def changeSet = ctx.currentCS()
        if (verb.isExpansion()) {
            log.info("${this.name} Adding Expansion $verb with $args to changeSet $changeSet")
            versionedExpansions[curExpansionVersion++] = new Tuple(verb, args, changeSet)
            return
        }
        if (verb.isContraction()) {
            log.info("${this.name} Adding Contraction $verb with $args to changeSet $changeSet")
            versionedContractions[curContractionVersion++] = new Tuple(verb, args, changeSet)
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
        versionedTransforms
    }

    def String toString() {
        "${getClass().simpleName}: $name, Expansions $versionedExpansions, Contractions $versionedContractions"
    }

}
