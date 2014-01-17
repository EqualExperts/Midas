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

    //Add
    @Test
    public void itValidatesSingleAddArgumentTypeAsJSON() {
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
    public void itValidatesMultipleAddFieldsWithinSingleArgumentTypesAsJSON() {
        //Given
        Grammar add = Grammar.add;
        List<String> args = Arrays.asList("{\"age\" : 0 , \"address.zip\" : 400001}");

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
    public void itDoesNotValidatesMultipleAddArgumentTypesAsJSON() {
        //Given
        Grammar add = Grammar.add;
        List<String> args = Arrays.asList("{\"age\" : 0 }", "{\"address.zip\" : 400001}");

        //When
        try {
            add.validate(args);

        //Then
            fail("Should not have passed for multiple JSON arguments");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
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

    @Test
    public void itDoesNotValidateNumericAddArgumentType() {
        //Given
        Grammar add = Grammar.add;
        List args = Arrays.asList(1,2.0);

        //When
        try {
            add.validate(args);
            fail("Should not have passed for the numeric input");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateBooleanAddArgumentType() {
        //Given
        Grammar add = Grammar.add;
        List args = Arrays.asList(true, false);

        //When
        try {
            add.validate(args);
            fail("Should not have passed for the boolean input");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    //Remove
    @Test
    public void itValidatesSingleRemoveArgumentTypeAsJSON() {
        //Given
        Grammar remove = Grammar.remove;
        List<String> args = Arrays.asList("[\"age\"]");

        //When
        try {
            remove.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for the valid JSON input");
        }
    }

    @Test
    public void itValidatesMultipleRemoveFieldsWithinSingleArgumentTypesAsJSON() {
        //Given
        Grammar remove = Grammar.remove;
        List<String> args = Arrays.asList("[\"age\", \"address.zip\"]");

        //When
        try {
            remove.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for the valid JSON input");
        }
    }

    @Test
    public void itDoesNotValidatesMultipleRemoveArgumentTypesAsJSON() {
        //Given
        Grammar remove = Grammar.remove;
        List<String> args = Arrays.asList("[\"age\"]","[\"address.zip\"]");

        //When
        try {
            remove.validate(args);

        //Then
            fail("Should not have passed for multiple JSON arguments");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateRemoveArgumentTypeAsInvalidJSON() {
        //Given
        Grammar remove = Grammar.remove;
        List<String> args = Arrays.asList("[age : 0]");

        //When
        try {
            remove.validate(args);
            fail("Should not have passed for the invalid JSON input");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateNumericRemoveArgumentType() {
        //Given
        Grammar remove = Grammar.remove;
        List args = Arrays.asList(1,2.0);

        //When
        try {
            remove.validate(args);
            fail("Should not have passed for the numeric input");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateBooleanRemoveArgumentType() {
        //Given
        Grammar remove = Grammar.remove;
        List args = Arrays.asList(true, false);

        //When
        try {
            remove.validate(args);
            fail("Should not have passed for the boolean input");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    //Copy
    @Test
    public void itDoesNotValidateSingleCopyArgumentType() {
        //Given
        Grammar copy = Grammar.copy;
        List<String> args = Arrays.asList("sourceField");

        //When
        try {
            copy.validate(args);
            fail("Should not have passed for single string input arg");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itValidatesTwoCopyArgumentTypes() {
        //Given
        Grammar copy = Grammar.copy;
        List<String> args = Arrays.asList("sourceField", "targetField");

        //When
        try {
            copy.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for two string input args");
        }
    }

    @Test
    public void itValidatesTwoCopyArgumentTypesWithSpaces() {
        //Given
        Grammar copy = Grammar.copy;
        List<String> args = Arrays.asList("source field", "target field");

        //When
        try {
            copy.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for two string input args");
        }
    }

    @Test
    public void itDoesNotValidateMoreThanTwoCopyArgumentTypes() {
        //Given
        Grammar copy = Grammar.copy;
        List<String> args = Arrays.asList("field1", "field2", "field3");

        //When
        try {
            copy.validate(args);
            fail("Should not have passed for a single argument.");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateNumericCopyArgumentTypes() {
        //Given
        Grammar copy = Grammar.copy;
        List args = Arrays.asList(1, 2.0, 3L);

        //When
        try {
            copy.validate(args);
            fail("Should not have passed for numeric arguments.");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateBooleanCopyArgumentTypes() {
        //Given
        Grammar copy = Grammar.copy;
        List args = Arrays.asList(true, false);

        //When
        try {
            copy.validate(args);
            fail("Should not have passed for boolean arguments.");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }


    //split
    @Test
    public void itDoesNotValidateSingleSplitArgumentType() {
        //Given
        Grammar split = Grammar.split;
        List<String> args = Arrays.asList("sourceField");

        //When
        try {
            split.validate(args);
            fail("Should not have passed for single string input arg");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateTwoSplitArgumentTypes() {
        //Given
        Grammar split = Grammar.split;
        List<String> args = Arrays.asList("sourceField", "targetField");

        //When
        try {
            split.validate(args);

        //Then
            fail("Should not have passed for two string input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeSplitArgumentsOfStringTypes() {
        //Given
        Grammar split = Grammar.split;
        List<String> args = Arrays.asList("sourceField", "targetField", "targetField2");

        //When
        try {
            split.validate(args);

        //Then
            fail("Should not have passed for three string input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeSplitArgumentsOfNumericTypes() {
        //Given
        Grammar split = Grammar.split;
        List args = Arrays.asList(1, 2, 3);

        //When
        try {
            split.validate(args);

        //Then
            fail("Should not have passed for three numeric input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeSplitArgumentsOfBooleanTypes() {
        //Given
        Grammar split = Grammar.split;
        List args = Arrays.asList(true, true, false);

        //When
        try {
            split.validate(args);

        //Then
            fail("Should not have passed for three boolean input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeSplitArgumentsOfJSONTypes() {
        //Given
        Grammar split = Grammar.split;
        List args = Arrays.asList("{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}");

        //When
        try {
            split.validate(args);

        //Then
            fail("Should not have passed for three JSON input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itValidatesThreeSplitArgumentsOfTypesStringStringAndJSON() {
        //Given
        Grammar split = Grammar.split;
        List<String> args = Arrays.asList("sourceField", " ", "{\"field1\": \"$1\", \"field2\": \"$2\"}");

        //When
        try {
            split.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for string, string, JSON input args");
        }
    }

    @Test
    public void itValidatesSourceFieldOfSplitArgumentTypesWithSpaces() {
        //Given
        Grammar split = Grammar.split;
        List<String> args = Arrays.asList("source field", " ", "{\"field1\": \"$1\", \"field2\": \"$2\"}");

        //When
        try {
            split.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for space separated sourceField input args");
        }
    }

    @Test
    public void itDoesNotValidateMoreThanThreeSplitArgumentTypes() {
        //Given
        Grammar split = Grammar.split;
        List<String> args = Arrays.asList("source field", " ", "{\"field1\": \"$1\", \"field2\": \"$2\"}", "extraArg");

        //When
        try {
            split.validate(args);
            fail("Should not have passed for four arguments.");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    //merge
    @Test
    public void itDoesNotValidateSingleMergeArgumentType() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List<String> args = Arrays.asList("sourceField");

        //When
        try {
            merge.validate(args);
            fail("Should not have passed for single string input arg");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void itDoesNotValidateTwoMergeArgumentTypes() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List<String> args = Arrays.asList("sourceField", "targetField");

        //When
        try {
            merge.validate(args);

        //Then
            fail("Should not have passed for two string input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeMergeArgumentsOfStringTypes() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List<String> args = Arrays.asList("sourceField", "targetField", "targetField2");

        //When
        try {
            merge.validate(args);

        //Then
            fail("Should not have passed for three string input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeMergeArgumentsOfNumericTypes() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List args = Arrays.asList(1, 2, 3);

        //When
        try {
            merge.validate(args);

        //Then
            fail("Should not have passed for three numeric input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeMergeArgumentsOfBooleanTypes() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List args = Arrays.asList(true, true, false);

        //When
        try {
            merge.validate(args);

        //Then
            fail("Should not have passed for three boolean input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itDoesNotValidateThreeMergeArgumentsOfJSONTypes() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List args = Arrays.asList("{\"arg1\" : \"value1\"}", "{\"arg2\" : \"value2\"}", "{\"arg3\":\"value3\"}");

        //When
        try {
            merge.validate(args);

        //Then
            fail("Should not have passed for three JSON input args");
        } catch (InvalidGrammar ig) {
            assertThat(true, is(true));
        }
    }

    @Test
    public void itValidatesThreeMergeArgumentsOfTypesStringStringAndJSON() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List<String> args = Arrays.asList("sourceField", " ", "{\"field1\": \"$1\", \"field2\": \"$2\"}");

        //When
        try {
            merge.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for string, string, JSON input args");
        }
    }

    @Test
    public void itValidatesSourceFieldOfMergeArgumentTypesWithSpaces() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List<String> args = Arrays.asList("source field", " ", "{\"field1\": \"$1\", \"field2\": \"$2\"}");

        //When
        try {
            merge.validate(args);

        //Then
            assertThat(true, is(true));
        } catch (InvalidGrammar ig) {
            fail("Should have passed for space separated sourceField input args");
        }
    }

    @Test
    public void itDoesNotValidateMoreThanThreeMergeArgumentTypes() {
        //Given
        Grammar merge = Grammar.mergeInto;
        List<String> args = Arrays.asList("source field", " ", "{\"field1\": \"$1\", \"field2\": \"$2\"}", "extraArg");

        //When
        try {
            merge.validate(args);
            fail("Should not have passed for four arguments.");
        } catch (InvalidGrammar ig) {

        //Then
            assertThat(ig, instanceOf(RuntimeException.class));
        }
    }
}
