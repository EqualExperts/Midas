package com.ee.midas.dsl.interpreter.representation;

public class InvalidGrammar extends RuntimeException {
    public InvalidGrammar() {
    }

    public InvalidGrammar(String message) {
        super(message);
    }

    public InvalidGrammar(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGrammar(Throwable cause) {
        super(cause);
    }
}
