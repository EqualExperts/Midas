package com.ee.midas.dsl.grammar.validator;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;
import groovy.json.JsonException;
import groovy.json.JsonSlurper;

public class JSONValidator implements Validator {
    private JsonSlurper jsonSlurper = new JsonSlurper();

    @Override
    public final void validate(final String arg) {
        try {
            jsonSlurper.parseText(arg);
        } catch (JsonException je) {
            throw new InvalidGrammar("MidasCompiler: Error: $je.message");
        }
    }
}
