package com.ee.midas.dsl.grammar.validator;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;
import groovy.json.JsonException;
import groovy.json.JsonSlurper;

public class JSONValidator implements Validator {
    private final JsonSlurper jsonSlurper = new JsonSlurper();
    private final String errMsg;

    public JSONValidator(final String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public final void validate(final String arg) {
        try {
            jsonSlurper.parseText(arg);
        } catch (JsonException je) {
            throw new InvalidGrammar(String.format(errMsg, arg));
        }
    }
}
