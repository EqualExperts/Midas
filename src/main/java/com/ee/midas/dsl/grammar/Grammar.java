package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;

import java.util.List;

public enum Grammar {
    @Expansion @ArgsSpecs(ArgType.JSON)
    add,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.Identifier })
    copy,

//    @Expansion   split,

    @Expansion   @ArgsSpecs({ ArgType.Identifier, ArgType.String, ArgType.JSON })
    mergeInto,

    @Contraction @ArgsSpecs(ArgType.JSON)
    remove;

    public void validate(final List<String> args) {
        try {
            ArgsSpecs argsSpecsAnnotation = Grammar.class
                                            .getField(name())
                                            .getAnnotation(ArgsSpecs.class);

            ArgType[] types = argsSpecsAnnotation.value();
            for (int index = 0; index < types.length; index++) {
                types[index].validate(args.get(index));
            }
        } catch (NoSuchFieldException e) {
            throw new InvalidGrammar(
                    "Sorry!! Midas Compiler bombed - " + e.getMessage());
        }
    }
}
