package com.ee.midas.utils


import org.specs2.mutable.Specification

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MemoizeSpecs extends Specification {
  "Memoizer" should {

    def square(x: Int) = x * x

    "evaluate a function correctly" in {
      //Given
      val memoizedSquare = Memoize(square)

      //When
      val x = 2
      val result = memoizedSquare(x)
      val expected = square(x)

      //Then
      result mustEqual expected
    }

    "ensures it calls a function with same arguments just once and memoizes for later use" in {
      //Given
      var called = 0
      def aFunction(x: Int): (Int, Int) = {
        called += 1
        (x, called)
      }

      val memoizedFunction = Memoize(aFunction)

      //When
      val x = 1
      val resultOnce = memoizedFunction(x)
      val resultTwice = memoizedFunction(x)

      //Then
      resultOnce mustEqual (x, 1)
      resultTwice mustEqual (x, 1)
    }

    "calls a function with different arguments just once and memoizes for later use" in {
      //Given
      var called = 0
      def aFunction(x: Int): (Int, Int) = {
        called += 1
        (x, called)
      }

      val memoizedFunction = Memoize(aFunction)

      //When
      val x = 1
      val resultXOnce = memoizedFunction(x)
      val resultXTwice = memoizedFunction(x)
      
      val y = 2
      val resultYOnce = memoizedFunction(y)
      val resultYTwice = memoizedFunction(y)

      //Then
      resultXOnce mustEqual (x, 1)
      resultXTwice mustEqual (x, 1)

      resultYOnce mustEqual (y, 2)
      resultYTwice mustEqual (y, 2)
    }
  }
}
