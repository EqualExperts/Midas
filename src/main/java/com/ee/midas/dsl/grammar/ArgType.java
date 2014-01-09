package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.grammar.validator.JSONValidator;
import com.ee.midas.dsl.grammar.validator.RegexValidator;
import com.ee.midas.dsl.grammar.validator.Validator;
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;

import java.util.regex.Pattern;

public enum ArgType {
    JSON(new JSONValidator()),
    String(new RegexValidator(Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*", Pattern.UNICODE_CASE)));

    private final Validator validator;

    ArgType(final Validator validator) {
        this.validator = validator;
    }

    public void validate(String arg) throws InvalidGrammar {
        validator.validate(arg);
    }
}