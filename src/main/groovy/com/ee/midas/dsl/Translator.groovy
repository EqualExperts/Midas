package com.ee.midas.dsl

import com.ee.midas.dsl.generator.Generator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.transform.TransformType

public class Translator<T> {
    private final Reader reader
    private final Generator<T> generator

    public Translator(Reader reader, Generator<T> generator) {
        this.reader = reader
        this.generator = generator
    }

    public T translate(final TransformType transformType, final List<File> deltaFiles) {
        def tree = reader.read(deltaFiles)
        generator.generate(transformType, tree)
    }
}
