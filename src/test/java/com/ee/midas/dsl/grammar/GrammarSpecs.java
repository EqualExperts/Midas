package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static junit.framework.Assert.fail;

@RunWith(JUnit4.class)
public class GrammarSpecs {
    @Test
    public void itValidatesAddArgumentTypeAsJSON() {
        //Given
        Grammar add = Grammar.add;
        List<String> args = Arrays.asList("{\"age\" : 0 }");
        //When
        try {
            add.validate(args);
            //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for the valid JSON input");
        }
    }

    @Test
    public void itDoesNotValidateAddArgumentTypeContainingInvalidJSON() {
        //Given
        Grammar add = Grammar.add;
        List<String> args = Arrays.asList("age : 0");
        //When
        try {
            add.validate(args);
            fail("Should not have passed for the invalid JSON input");
        } catch (InvalidGrammar ig) {
            //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    //TODO: Add specs for remove on similar lines
    // - single field removal
    // - multiple field removal
    // - invalid json failure

}
