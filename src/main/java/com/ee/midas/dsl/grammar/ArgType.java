package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.grammar.validator.JSONValidator;
import com.ee.midas.dsl.grammar.validator.RegexValidator;
import com.ee.midas.dsl.grammar.validator.Validator;

import java.util.regex.Pattern;

public enum ArgType {
    JSON(new JSONValidator("%s is an invalid json: %s")),
    Identifier(new RegexValidator(Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*",
                              Pattern.UNICODE_CASE),
                              "%s needs to be a valid identifier!")),
    //Letters = \p{L}
    //Numbers = \p{N}
    //Symbols = \p{S}
    //Punctuation = \p{P}
    //Any Whitespace or Invisible character = \p{Z}
    String(new RegexValidator(Pattern.compile("[\\p{L}\\p{N}\\p{S}\\p{P}\\p{Z}]*",
                               Pattern.UNICODE_CASE),
                               "%s needs to be a valid string!"));

    private final Validator validator;

    ArgType(final Validator validator) {
        this.validator = validator;
    }

    public void validate(final String arg) {
        validator.validate(arg);
    }
}
