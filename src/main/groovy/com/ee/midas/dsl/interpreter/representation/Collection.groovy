package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.transform.TransformType
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import static com.ee.midas.transform.TransformType.CONTRACTION
import static com.ee.midas.transform.TransformType.EXPANSION
import static scala.collection.JavaConverters.*
import scala.Tuple3

@ToString
@Slf4j
class Collection {
    final String name
    private final Map<Double, Tuple> versionedExpansions = [:] as LinkedHashMap
    private final Map<Double, Tuple> versionedContractions = [:] as LinkedHashMap
    private Double curExpansionVersion = 1
    private Double curContractionVersion = 1
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
        Map<Double, Tuple> versionedTransforms = null
        if(transformType == EXPANSION) {
            versionedTransforms = versionedExpansions
        }

        if(transformType == CONTRACTION) {
            versionedTransforms = versionedContractions
        }
        versionedTransforms
    }

    scala.collection.mutable.Map<Double, Tuple3<Verb, List<String>, Long>> asVersionedScalaMap(TransformType transformType) {
        Map<Double, Tuple> versionedTransforms = null
        if(transformType == EXPANSION) {
            versionedTransforms = versionedExpansions
        }

        if(transformType == CONTRACTION) {
            versionedTransforms = versionedContractions
        }
        def scalizedMapContents = versionedTransforms.collectEntries { double version, Tuple values ->
           def (Verb verb, java.util.List<String> args, long changeSet) = values
           [version, new Tuple3(verb, args, changeSet)]
        }
        mapAsScalaMapConverter(scalizedMapContents).asScala()
    }

    def String toString() {
        "${getClass().simpleName}: $name, Expansions $versionedExpansions, Contractions $versionedContractions"
    }

}
