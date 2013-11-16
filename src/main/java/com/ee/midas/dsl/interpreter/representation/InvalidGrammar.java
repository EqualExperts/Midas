package com.ee.midas.dsl.interpreter.representation;

public class InvalidGrammar extends RuntimeException {
    public InvalidGrammar() {
    }

    public InvalidGrammar(final String message) {
        super(message);
    }

    public InvalidGrammar(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidGrammar(final Throwable cause) {
        super(cause);
    }
}
