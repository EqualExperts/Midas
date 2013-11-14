package com.ee.midas.dsl

import com.ee.midas.dsl.generator.Generator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator


class Translator {
    private def reader = new Reader()
    private Generator generator = new ScalaGenerator()

    def translate(final List<File> deltaFiles) {
        def tree = reader.read(deltaFiles)
        generator.generate(tree)
    }
}
