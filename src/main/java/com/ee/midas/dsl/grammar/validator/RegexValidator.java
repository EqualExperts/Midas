package com.ee.midas.dsl.grammar.validator;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValidator implements Validator {
    private final Pattern regex;

    public RegexValidator(final Pattern regex) {
        this.regex = regex;
    }

    @Override
    public final void validate(final String arg) {
        Matcher matcher = regex.matcher(arg);
        if (!matcher.matches()) {
            throw new InvalidGrammar("$arg does not match $regex");
        }
    }
}
