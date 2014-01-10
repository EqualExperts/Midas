package com.ee.midas.dsl.grammar.validator;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;
import com.mongodb.util.JSONParseException;
import com.mongodb.util.JSON;

public class JSONValidator implements Validator {
    private final String errMsg;

    public JSONValidator(final String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public final void validate(final String arg) {
        try {
            JSON.parse(arg);
        } catch (JSONParseException je) {
            throw new InvalidGrammar(String.format(errMsg, arg, je.getMessage()));
        }
    }
}
