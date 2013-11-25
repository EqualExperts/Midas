package com.ee.midas.dsl

import com.ee.midas.dsl.generator.Generator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.interpreter.representation.Tree

import spock.lang.Specification

public class TranslatorSpecs extends Specification {

    def "Translator Reads From Files"() {

        given: "a generator, reader and a translator"
        def mockGenerator = Mock(Generator)
        def mockReader = Mock(Reader)
        def tree = Stub(Tree)
        def translator = new Translator(mockReader, mockGenerator)
        def deltaFiles = Stub(List)

        when: "it translates delta files"
        translator.translate(deltaFiles)

        then: "it reads the files and generates a tree"
        1 * mockReader.read(deltaFiles) >> tree
        1 * mockGenerator.generate(tree)
    }
}
