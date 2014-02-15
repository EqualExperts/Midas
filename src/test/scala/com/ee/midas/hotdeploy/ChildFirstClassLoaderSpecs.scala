package com.ee.midas.hotdeploy

import org.specs2.mutable.{BeforeAfter, Specification}
import com.ee.midas.Main
import java.io.{FileWriter, File}
import com.ee.midas.utils.ScalaCompiler
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ChildFirstClassLoaderSpecs extends Specification with ScalaCompiler {

  val loader = Main.getClass.getClassLoader
  val resourceDir = new File("src/test/scala/resources")
  val srcScalaPath = new File(resourceDir.getAbsolutePath + "/generated/scala")
  val binScalaPath = new File(resourceDir.getAbsolutePath + "/generated/scala/bin")


  val srcScalaDirURI = "generated/scala/"
  val srcScalaFilename = "someScalaObject.scala"
  val binDirURI = "generated/scala/bin/"
  val classpathURI = "."

  val classpathDir = loader.getResource(classpathURI)
  val binDir = loader.getResource(binDirURI)
  val srcScalaDir = loader.getResource(srcScalaDirURI)
  val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

  trait SetupTeardown extends BeforeAfter {
    def before: Any = {
      binScalaPath.mkdirs()
    }

    def after: Any = {
      binScalaPath.delete
    }

  }

  "Child First Class Loader" should {
    "load the given class" in new SetupTeardown {
      //given
      if(!srcScalaFile.exists()) {
        srcScalaFile.createNewFile()
        val writer = new FileWriter(srcScalaFile)
        writer.write("object TestObject {println(\"Hello World\")}")
        writer.close()
      }
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)

      //when
      val classLoader = new ChildFirstClassLoader(Array(binDir),getClass.getClassLoader)

      //then
      classLoader.loadClass("TestObject").getName mustEqual "TestObject"
    }
  }
}
