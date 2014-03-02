package com.ee.midas.dsl.expressions

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FunctionSpecs extends Specification with DataTables {
  "stringify just like how it is written" ^ {
            "function"                 |  "functionString"                     |
              Add()                    !  "Add()"                              |
      Add(Literal(2), Literal(0))      !  "Add(Literal(2), Literal(0))"        |
      Divide(Literal(2), Literal(0))   !  "Divide(Literal(2), Literal(0))"     |
      Subtract(Literal(1), Literal(2)) !  "Subtract(Literal(1), Literal(2))"   |
      Multiply(Literal(1), Literal(2)) !  "Multiply(Literal(1), Literal(2))"   |>
      { (function, functionString) =>  function.toString mustEqual functionString }
  }
}
