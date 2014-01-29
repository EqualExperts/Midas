package com.ee.midas.dsl.generator

import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.transform.TransformType

public interface Generator<T> {
    T generate(TransformType transformType, Tree representation)
}