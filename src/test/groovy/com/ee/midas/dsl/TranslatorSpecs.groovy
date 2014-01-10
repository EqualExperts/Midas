package com.ee.midas.dsl

import com.ee.midas.dsl.generator.Generator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.interpreter.representation.Tree
import static com.ee.midas.transform.TransformType.*
import spock.lang.Specification

public class TranslatorSpecs extends Specification {

    def "Translator Reads Expansion Files"() {

        given: "a generator, reader and a translator"
            Generator mockGenerator = Mock(Generator)
            Reader mockReader = Mock(Reader)
            def tree = Stub(Tree)
            def translator = new Translator(mockReader, mockGenerator)
            def deltaFiles = Stub(List)

        when: "it translates delta files for expansion mode"
            translator.translate(EXPANSION, deltaFiles)

        then: "it reads the files and generates a tree"
            1 * mockReader.read(deltaFiles) >> tree
            1 * mockGenerator.generate(EXPANSION, tree)
    }

    def "Translator Reads Contraction Files"() {
        given: "a generator, reader and a translator"
            Generator mockGenerator = Mock(Generator)
            Reader mockReader = Mock(Reader)
            def tree = Stub(Tree)
            def translator = new Translator(mockReader, mockGenerator)
            def deltaFiles = Stub(List)

        when: "it translates delta files"
            translator.translate(CONTRACTION, deltaFiles)

        then: "it reads the files and generates a tree"
            1 * mockReader.read(deltaFiles) >> tree
            1 * mockGenerator.generate(CONTRACTION, tree)
    }

}
