/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

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
