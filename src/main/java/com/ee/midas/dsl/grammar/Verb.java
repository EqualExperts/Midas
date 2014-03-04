package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.interpreter.representation.InvalidGrammar;

import java.lang.annotation.Annotation;
import java.util.List;

public enum Verb {
    @Expansion @ArgsSpecs(ArgType.JSON)
    add,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.Identifier })
    copy,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.String, ArgType.JSON })
    split,

    @Expansion @ArgsSpecs({ ArgType.JSON, ArgType.String, ArgType.Identifier })
    merge,

    @Expansion @ArgsSpecs({ ArgType.Identifier, ArgType.JSON })
    transform,

    @Contraction @ArgsSpecs(ArgType.JSON)
    remove;

    private Annotation getAnnotation(
            final Class<? extends Annotation> annotationClass) {
        try {
            return Verb.class
                .getField(name())
                .getAnnotation(annotationClass);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public boolean isExpansion() {
        return getAnnotation(Expansion.class) != null;
    }

    public boolean isContraction() {
        return getAnnotation(Contraction.class) != null;
    }

    public void validate(final List<String> args) {
        ArgsSpecs annotation = (ArgsSpecs) getAnnotation(ArgsSpecs.class);
        if (annotation == null) {
            throw new InvalidGrammar(
                "You seem to have forgotten @ArgsSpecs on verb " + name());
        }
        ArgType[] types = annotation.value();
        validateArgsLength(args, types);
        validateArgsValues(args, types);
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
