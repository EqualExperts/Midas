package com.ee.midas.dsl

import com.ee.midas.dsl.generator.Generator
import com.ee.midas.dsl.interpreter.Reader

public class Translator {
    private final Reader reader
    private final Generator generator

    public Translator(Reader reader, Generator generator) {
        this.reader = reader
        this.generator = generator
    }

    public String translate(final List<File> deltaFiles) {
        def tree = reader.read(deltaFiles)
        generator.generate(tree)
    }
}
