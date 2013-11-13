package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Databases

public interface Generator {
    String generate(Databases representation)
}