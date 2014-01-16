package com.ee.midas.dsl.grammar.validator

import com.ee.midas.dsl.grammar.Grammar
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import spock.lang.Specification


public class GrammarSpecs extends Specification {

    def itValidatesAddArgumentTypeAsJSON() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add
            def args = ["""{"age" : 0 }"""]

        when: 'the verb validates the arguments supplied'
            add.validate(args)

        then:
            notThrown(InvalidGrammar)
    }

    def itDoesNotValidateAddArgumentTypeContainingInvalidJSON() {
        given: 'Add verb from Grammar'
            Grammar add = Grammar.add
            def args = ["""age : 0"""]

        when: 'the verb validates the arguments supplied'
            add.validate(args);

        then:
            thrown(InvalidGrammar)
    }

    //TODO: Add specs for remove on similar lines
    // - single field removal
    // - multiple field removal
    // - invalid json failure

}
