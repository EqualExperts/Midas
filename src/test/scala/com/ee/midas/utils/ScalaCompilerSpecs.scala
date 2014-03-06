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
import java.io.{File, FileWriter}
import com.ee.midas.Main

@RunWith(classOf[JUnitRunner])
object ScalaCompilerSpecs extends Specification with ScalaCompiler {

  sequential

  val loader = Main.getClass.getClassLoader

  val srcScalaDirURI = "generated/scala/"
  val srcScalaFilename = "ScalaCompilerTest.scala"
  val binDirURI = "generated/scala/bin/"
  val classpathURI = "."

  val classpathDir = loader.getResource(classpathURI)
  val binDir = loader.getResource(binDirURI)
  val srcScalaDir = loader.getResource(srcScalaDirURI)
  val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

  "Scala Compiler" should {
    "throw exception in case of errors" in {
      //Given: A scala file that doesn't exist
      if(srcScalaFile.exists()) {
        srcScalaFile.delete()
      }

      //When: The file is compiled
      //Then: RuntimeException must be thrown
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath) must throwA[RuntimeException]
    }

    "compile successfully" in {
      //Given: A scala file with some scala code
      if(!srcScalaFile.exists()) {
        srcScalaFile.createNewFile()
        val writer = new FileWriter(srcScalaFile)
        writer.write(s"""class ScalaCompilerTest {println("Hello World")}""")
        writer.close()
      }

      //When: The file is compiled
      //Then: No exception must be thrown and compilation must succeed
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath) must throwA[RuntimeException].not
    }
  }

}
