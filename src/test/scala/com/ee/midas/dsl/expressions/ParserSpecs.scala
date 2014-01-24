package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ParserSpecs extends Specification {

  trait ExpressionParser extends Parser with Scope {
    def Result[T](result: ParseResult[T]): T = result match {
      case Success(value, _) =>  value
      case NoSuccess(message, _) => throw new IllegalArgumentException(s"Parsing Failed Message: $message")
    }
  }

  "Parse" should {
    "return Literal for null" in new ExpressionParser {
      //Given
      val input = "null"

      //When
      val expression = Result(parseAll(value, input))
      
      //Then
      expression mustEqual Literal(null)
    }

    "return Literal for true" in new ExpressionParser {
      //Given
      val input = "true"

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Literal(true)
    }

    "return Literal for false" in new ExpressionParser {
      //Given
      val input = "false"

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Literal(false)
    }

    "return Literal for Integer" in new ExpressionParser {
      //Given
      val input = "2"

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Literal(2)
    }

    "return Literal for Decimal" in new ExpressionParser {
      //Given
      val input = "2.4"

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Literal(2.4)
    }

    "return Field for fieldName" in new ExpressionParser {
      //Given
      val input = """"$age""""

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Field("age")
    }

    "return Field for level-1 nested fieldName" in new ExpressionParser {
      //Given
      val input = """"$address.zip""""

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Field("address.zip")
    }

    "return Field for level-2 nested fieldName" in new ExpressionParser {
      //Given
      val input = """"$address.line.1""""

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Field("address.line.1")
    }

    "fail to parse field that has trailing dot" in new ExpressionParser {
      //Given
      val input = """"$address.""""

      //When-Then
      Result(parseAll(value, input)) must throwA[IllegalArgumentException]
    }

    "fail to parse field that has extra dot between levels" in new ExpressionParser {
      //Given
      val input = """"$address..line""""

      //When-Then
      Result(parseAll(value, input)) must throwA[IllegalArgumentException]
    }

    "fail to parse field begins with a dot" in new ExpressionParser {
      //Given
      val input = """"$.address""""

      //When-Then
      Result(parseAll(value, input)) must throwA[IllegalArgumentException]
    }

    "fail to parse field that is prefixed with multiple $" in new ExpressionParser {
      //Given
      val input = """"$$address""""

      //When-Then
      Result(parseAll(value, input)) must throwA[IllegalArgumentException]
    }

    "return Literal for string" in new ExpressionParser {
      //Given
      val input = """"age""""

      //When
      val expression = Result(parseAll(value, input))

      //Then
      expression mustEqual Literal("age")
    }

    "return function name" in new ExpressionParser {
      //Given
      val funcName = "function"
      val input = "$" + funcName

      //When
      val functionName = Result(parseAll(fnName, input))

      //Then
      funcName mustEqual functionName
    }

    "fail to parse function name containing reserved prefix $" in new ExpressionParser {
      //Given
      val funcName = "$function"
      val input = "$" + funcName

      //When-Then
      Result(parseAll(fnName, input)) must throwA[IllegalArgumentException]
    }

    "return empty args" in new ExpressionParser {
      //Given
      val input = "[]"

      //When
      val args = Result(parseAll(fnArgs, input))

      //Then
      args must beEmpty
    }

    "return Number argument" in new ExpressionParser {
      //Given
      val input = "[1]"

      //When
      val args = Result(parseAll(fnArgs, input))

      //Then
      args mustEqual List(Literal(1))
    }

    "return Number and String args" in new ExpressionParser {
      //Given
      val input = """[1, "age"]"""

      //When
      val args = Result(parseAll(fnArgs, input))

      //Then
      args mustEqual List(Literal(1), Literal("age"))
    }

    "return Number, String and Field args" in new ExpressionParser {
      //Given
      val input = """[1, "age", "$name"]"""

      //When
      val args = Result(parseAll(fnArgs, input))

      //Then
      args mustEqual List(Literal(1), Literal("age"), Field("name"))
    }

    "return Function arg" in new ExpressionParser {
      //Given
      val input = """[{ $add: [1, "$age"]}]"""

      //When
      val args = Result(parseAll(fnArgs, input))

      //Then
      args mustEqual List(Add(Literal(1), Field("age")))
    }

    "fail to parse incomplete args" in new ExpressionParser {
      //Given
      val input = "[1"

      //When-Then
      Result(parseAll(fnArgs, input)) must throwA[IllegalArgumentException]
    }

    "fail to parse ill-formed args" in new ExpressionParser {
      //Given
      val input = "[1]]"

      //When-Then
      Result(parseAll(fnArgs, input)) must throwA[IllegalArgumentException]
    }

    "return Add function" in new ExpressionParser {
      //Given
      val input = """$add: [1, "$age"]"""

      //When
      val add = Result(parseAll(fn, input))

      //Then
      add mustEqual Add(Literal(1), Field("age"))
    }

    "return empty Add function" in new ExpressionParser {
      //Given
      val input = """$add: []"""

      //When
      val add = Result(parseAll(fn, input))

      //Then
      add mustEqual Add()
    }

    "return recursive add function" in new ExpressionParser {
      //Given
      val input = """$add: [1, { $add: [2, "$age"]}]"""

      //When
      val add = Result(parseAll(fn, input))

      //Then
      add mustEqual Add(Literal(1), Add(Literal(2), Field("age")))
    }
    
    "return Multiply function" in new ExpressionParser {
      //Given
      val input = """$multiply: [1, "$age"]"""

      //When
      val multiply = Result(parseAll(fn, input))

      //Then
      multiply mustEqual Multiply(Literal(1), Field("age"))
    }

    "return empty multiply function" in new ExpressionParser {
      //Given
      val input = """$multiply: []"""

      //When
      val multiply = Result(parseAll(fn, input))

      //Then
      multiply mustEqual Multiply()
    }

    "return recursive multiply function" in new ExpressionParser {
      //Given
      val input = """$multiply: [1, { $multiply: [2, "$age"]}]"""

      //When
      val multiply = Result(parseAll(fn, input))

      //Then
      multiply mustEqual Multiply(Literal(1), Multiply(Literal(2), Field("age")))
    }
    
    "return concat function" in new ExpressionParser {
      //Given
      val input = """$concat: [1, "$age"]"""

      //When
      val concat = Result(parseAll(fn, input))

      //Then
      concat mustEqual Concat(Literal(1), Field("age"))
    }

    "return empty concat function" in new ExpressionParser {
      //Given
      val input = """$concat: []"""

      //When
      val concat = Result(parseAll(fn, input))

      //Then
      concat mustEqual Concat()
    }

    "return recursive concat function" in new ExpressionParser {
      //Given
      val input = """$concat: [1, { $concat: [2, "$age"]}]"""

      //When
      val concat = Result(parseAll(fn, input))

      //Then
      concat mustEqual Concat(Literal(1), Concat(Literal(2), Field("age")))
    }

    "allow single function at top-level within obj" in new ExpressionParser {
      //Given
      val input = """{ $add: [1, { $multiply: [2, "$age"]}] }"""

      //When
      val objExpr = Result(parseAll(obj, input))

      //Then
      objExpr mustEqual Add(Literal(1), Multiply(Literal(2), Field("age")))
    }

    "fail when more than 1 function is defined at top-level in obj" in new ExpressionParser {
      //Given
      val input = """{ $add: [1, { $multiply: [2, "$age"]}], $multiply: [] }"""

      //When-Then
      Result(parseAll(obj, input)) must throwA[IllegalArgumentException]
    }

    "return Subtract function" in new ExpressionParser {
      //Given
      val input = """$subtract: [1, "$age"]"""

      //When
      val subtract = Result(parseAll(fn, input))

      //Then
      subtract mustEqual Subtract(Literal(1), Field("age"))
    }

    "return empty Subtract function" in new ExpressionParser {
      //Given
      val input = """$subtract: []"""

      //When
      val subtract = Result(parseAll(fn, input))

      //Then
      subtract mustEqual Subtract()
    }

    "return Subtract function with 1 argument" in new ExpressionParser {
      //Given
      val input = """$subtract: [1.0]"""

      //When
      val subtract = Result(parseAll(fn, input))

      //Then
      subtract mustEqual Subtract(Literal(1d))
    }

    "return recursive subtract function" in new ExpressionParser {
      //Given
      val input = """$subtract: [1, { $subtract: [2, "$age"]}]"""

      //When
      val subtract = Result(parseAll(fn, input))

      //Then
      subtract mustEqual Subtract(Literal(1), Subtract(Literal(2), Field("age")))
    }

  }
}