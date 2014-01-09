package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: dhavald
 * Date: 09/01/14
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
class RegexValidator implements Validator {
    private final Pattern regex

    public RegexValidator(Pattern regex) {
        this.regex = regex
    }

    @Override
    void validate(String arg) throws InvalidGrammar {
        Matcher matcher = regex.matcher(arg)
        if(matcher.matches() == false) {
            throw new InvalidGrammar("$arg does not match $regex")
        }
    }
}
