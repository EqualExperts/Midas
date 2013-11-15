package com.ee.midas.dsl

import com.ee.midas.dsl.generator.Generator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator


class Translator {
    private final Reader reader
    private final Generator generator

    public Translator(reader, generator) {
        this.reader = reader
        this.generator = generator
    }

    def translate(final List<File> deltaFiles) {
        def tree = reader.read(deltaFiles)
        generator.generate(tree)
    }
}
