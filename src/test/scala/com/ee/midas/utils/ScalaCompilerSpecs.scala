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
