package com.ee.midas.dsl.grammar.validator;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;

public interface Validator {
    void validate(String arg) throws InvalidGrammar;
}
