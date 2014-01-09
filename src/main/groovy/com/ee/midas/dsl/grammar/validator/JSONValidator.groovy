package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class JSONValidator implements Validator {
    private JsonSlurper jsonSlurper = new JsonSlurper()

    @Override
    void validate(String arg) throws InvalidGrammar {
        try {
            log.debug("Parsing Input JSON...$arg")
            jsonSlurper.parseText(arg)
            log.debug("Parsed Input JSON Successfully")
        } catch (JsonException je) {
            throw new InvalidGrammar("MidasCompiler: Error: $je.message")
        }
    }
}
