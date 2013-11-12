package com.ee.midas.applier

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AssemblerSpecs extends Specification {

   "Assembler" should {
      "assemble two buffers" in {
        val testBuffer1 : Array[Int] = Array(1,2,3,4)
        val testBuffer2 : Array[Int] = Array(5,6,7,8)
        val expectedResult : Array[Int] = Array(1,2,3,4,5,6,7,8)

        val actualResult = Assembler.assemble[Int](testBuffer1, testBuffer2)

        expectedResult mustEqual  actualResult
      }
   }
}
