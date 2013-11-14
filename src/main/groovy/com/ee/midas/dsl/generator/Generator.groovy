package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Tree

public interface Generator {
    String generate(Tree representation)
}