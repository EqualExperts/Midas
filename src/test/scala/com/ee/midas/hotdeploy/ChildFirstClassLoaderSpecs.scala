package com.ee.midas.hotdeploy

import org.specs2.mutable.Specification
import com.ee.midas.Main
import java.io.{FileWriter, File}
import com.ee.midas.utils.ScalaCompiler
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ChildFirstClassLoaderSpecs extends Specification with ScalaCompiler {

  val loader = Main.getClass.getClassLoader

  val srcScalaDirURI = "generated/scala/"
  val srcScalaFilename = "someScalaObject.scala"
  val binDirURI = "generated/scala/bin/"
  val classpathURI = "."

  val classpathDir = loader.getResource(classpathURI)
  val binDir = loader.getResource(binDirURI)
  val srcScalaDir = loader.getResource(srcScalaDirURI)
  val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

  "Child First Class Loader" should {
    "load the given class" in {
      if(!srcScalaFile.exists()) {
        srcScalaFile.createNewFile()
        val writer = new FileWriter(srcScalaFile)
        writer.write("object TestObject {println(\"Hello World\")}")
        writer.close()
      }
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)
      val classLoader = new ChildFirstClassLoader(Array(binDir),getClass.getClassLoader)
      classLoader.loadClass("TestObject").getName mustEqual "TestObject"
    }
  }
}
