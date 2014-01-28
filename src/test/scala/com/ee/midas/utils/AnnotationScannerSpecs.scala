package com.ee.midas.utils

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.TestAnnotation
import com.ee.midas.dsl.expressions.FunctionExpression

@RunWith(classOf[JUnitRunner])
class AnnotationScannerSpecs extends Specification {
  "AnnotationScanner" should {
    "scan for classes in specified package for given annotation" in {
      //Given
      val scanner = new AnnotationScanner("com.ee.midas", classOf[FunctionExpression])

      //When
      val classes = scanner.scan

      //Then
      classes must not be empty
      classes must contain("com.ee.midas.dsl.expressions.Add").exactly(1)
    }

    "return empty when scanning for classes in specified package that do not have the given annotation" in {
      //Given
      val scanner = new AnnotationScanner("com.ee.midas", classOf[TestAnnotation])

      //When
      val classes = scanner.scan

      //Then
      classes must be empty
    }
  }
}
