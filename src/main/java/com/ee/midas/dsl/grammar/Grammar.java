package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;

import java.util.List;

public enum Grammar {
    @Expansion @ArgsSpecs(ArgType.JSON)
    add,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.Identifier })
    copy,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.String, ArgType.JSON })
    split,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.String, ArgType.JSON })
    mergeInto,

    @Contraction @ArgsSpecs(ArgType.JSON)
    remove;

    public void validate(final List<String> args) {
        try {
            ArgsSpecs argsSpecsAnnotation = Grammar.class
                                            .getField(name())
                                            .getAnnotation(ArgsSpecs.class);

            ArgType[] types = argsSpecsAnnotation.value();
            validateArgsLength(args, types);
            validateArgsValues(args, types);
        } catch (NoSuchFieldException e) {
            throw new InvalidGrammar(
                    "Sorry!! Midas Compiler bombed - " + e.getMessage());
        } catch (ClassCastException e) {
            throw new InvalidGrammar(
                    "Please check the type of arguments - " + e.getMessage());
        }
    }

    private void validateArgsValues(final List<String> args,
                                    final ArgType[] types) {
        for (int index = 0; index < types.length; index++) {
            types[index].validate(args.get(index));
        }
    }

    private void validateArgsLength(final List<String> args,
                                    final ArgType[] types) {
        if (types.length != args.size()) {
            final String errMsg =
                "Wrong number of arguments supplied for %s, "
                + "Required %d, Found %d";
            throw new InvalidGrammar(String.format(errMsg,
                                                    name(),
                                                    types.length,
                                                    args.size()));
        }
    }
}
