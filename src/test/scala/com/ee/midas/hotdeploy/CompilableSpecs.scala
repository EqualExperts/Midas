package com.ee.midas.hotdeploy

import org.specs2.mutable.Specification

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.{File, FileWriter}
import com.ee.midas.utils.Compilable
import com.ee.midas.Main

@RunWith(classOf[JUnitRunner])
object CompilableSpecs extends Specification with Compilable {

  sequential

  val loader = Main.getClass.getClassLoader

  val deltasDirURI = "deltas/"
  val srcScalaTemplateURI = "templates/Transformations.scala.template"
  val srcScalaDirURI = "generated/scala/"
  val srcScalaFilename = "Transformations.scala"
  val binDirURI = "generated/scala/bin/"
  val clazzName = "com.ee.midas.transform.Transformations"
  val classpathURI = "."

  val classpathDir = loader.getResource(classpathURI)
  val binDir = loader.getResource(binDirURI)
  val deltasDir = loader.getResource(deltasDirURI)
  val srcScalaTemplate = loader.getResource(srcScalaTemplateURI)
  val srcScalaDir = loader.getResource(srcScalaDirURI)
  log.info(s"Source Scala Dir = $srcScalaDir")
  val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

  "Compilable class" should {
    "throw exception in case of errors" in {
      if(srcScalaFile.exists()) {
        srcScalaFile.delete()
      }
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath) must throwA[RuntimeException]
    }

    "compile successfully" in {
      if(!srcScalaFile.exists()) {
        srcScalaFile.createNewFile()
        val writer = new FileWriter(srcScalaFile)
        writer.write("object simpleObject {println(\"Hello World\")}")
        writer.close()
      }
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath) must throwA[RuntimeException].not
    }
  }

}
