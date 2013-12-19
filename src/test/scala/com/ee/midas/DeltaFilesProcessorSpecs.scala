package com.ee.midas

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.hotdeploy.DeployableHolder
import com.ee.midas.transform.{Transformations, Transforms}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import java.net.URL
import java.io.{FileWriter, PrintWriter, File, Writer}
import org.specs2.mock.Mockito

@RunWith(classOf[JUnitRunner])
class DeltaFilesProcessorSpecs extends Specification with Mockito {

     "Delta File Processor" should {
         "process delta files " in {

           val loader = com.ee.midas.Main.getClass.getClassLoader

           val deltasDirURI = "deltas/"
           val deltasDir = loader.getResource(deltasDirURI)

           val srcScalaTemplateURI = "templates/Transformations.scala.template"
           val srcScalaTemplate = loader.getResource(srcScalaTemplateURI)

           val srcScalaDirURI = "generated/scala/"
           val srcScalaDir = loader.getResource(srcScalaDirURI)
           val srcScalaFilename = "FileProcessorTest.scala"
           val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

           val binDirURI = "generated/scala/bin/"
           val binDir = loader.getResource(binDirURI)

           val classpathURI = "."
           val classpathDir = loader.getResource(classpathURI)

           val clazzName = "com.ee.midas.FileProcessorTest"

           val writer = new PrintWriter(srcScalaFile, "utf-8")

           val deployableHolder = new DeployableHolder[Transforms] {
             def createDeployable: Transforms = new FileProcessorTest
           }

           val deltaFileProcessor = new DeltaFilesProcessor(new Translator(new Reader(), new ScalaGenerator()), deployableHolder)

           deltaFileProcessor.process(deltasDir, srcScalaTemplate, writer, srcScalaFile,
             binDir, clazzName, classpathDir)

           writer.close

           val deployable = deployableHolder.get
           deployable.contractions.contains("field")
           deployable.expansions.contains("field")
           deployable.isInstanceOf[FileProcessorTest]
         }
     }
}
