package com.ee.midas

import org.specs2.mutable.{BeforeAfter, Specification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.hotdeploy.DeployableHolder
import com.ee.midas.transform.{Transformations, Transforms}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import java.io.{PrintWriter, File}
import org.specs2.mock.Mockito
import com.ee.midas.transform.TransformType._

@RunWith(classOf[JUnitRunner])
class DeltaFilesProcessorSpecs extends Specification with Mockito {
     trait SetupTeardown extends BeforeAfter {
       val loader = com.ee.midas.Main.getClass.getClassLoader
       val myDeltas = new File("src/test/scala/com/ee/midas/myDeltas")
       val deltasDirURL =  myDeltas.toURI.toURL
       val srcScalaTemplateURI = "templates/Transformations.scala.template"
       val srcScalaDirURI = "generated/scala/"
       val binDirURI = "generated/scala/bin/"
       val classpathURI = "."
       val srcScalaDir = loader.getResource(srcScalaDirURI)

       val srcScalaFilename = "Transformations.scala"
       val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

       val writer = new PrintWriter(srcScalaFile, "utf-8")
       val clazzName = "com.ee.midas.transform.Transformations"
       val srcScalaTemplate = loader.getResource(srcScalaTemplateURI)

       val binDir = loader.getResource(binDirURI)
       val classpathDir = loader.getResource(classpathURI)

       val expansionDeltaFile = new File(myDeltas.getPath + "/expansion.delta")
       val contractionDeltaFile = new File(myDeltas.getPath + "/contraction.delta")

       val deployableHolder = new DeployableHolder[Transforms] {
         def createDeployable: Transforms = new Transformations
       }

       def before: Any = {
         myDeltas.mkdir()
         val expansionDelta = new PrintWriter(expansionDeltaFile)
         val contractionDelta = new PrintWriter(contractionDeltaFile)

         expansionDelta.write("use someDatabase\n")
         expansionDelta.write("db.collection.add(\'{\"field\": \"value\"}\')\n")
         expansionDelta.flush()
         expansionDelta.close()

         contractionDelta.write("use someDatabase\n")
         contractionDelta.write("db.collection.remove(\'[\"field\"]\')\n")
         contractionDelta.flush()
         contractionDelta.close()
       }

       def after: Any = {
         contractionDeltaFile.delete
         expansionDeltaFile.delete
         myDeltas.delete
       }
     }

     sequential
     "Delta File Processor" should {
         "process expansion delta files " in new SetupTeardown {
           //Given
           val deltaFileProcessor = new DeltaFilesProcessor(new Translator(new Reader(), new ScalaGenerator()), deployableHolder)

           //When
           deltaFileProcessor.process(EXPANSION, deltasDirURL, srcScalaTemplate, writer, srcScalaFile,
             binDir, clazzName, classpathDir)

           writer.close

           //Then
           val deployable = deployableHolder.get
           deployable.responseContractions must beEmpty
           val expansions = deployable.responseExpansions
           expansions must haveLength(1)
           expansions must haveKey("someDatabase.collection")
         }

         "process contraction delta files " in new SetupTeardown {
           //Given
           val deltaFileProcessor = new DeltaFilesProcessor(new Translator(new Reader(), new ScalaGenerator()), deployableHolder)

           //When
           deltaFileProcessor.process(CONTRACTION, deltasDirURL, srcScalaTemplate, writer, srcScalaFile,
             binDir, clazzName, classpathDir)

           writer.close

           //Then
           val deployable = deployableHolder.get
           deployable.responseExpansions must beEmpty
           val contractions = deployable.responseContractions
           contractions must haveLength(1)
           contractions must haveKey("someDatabase.collection")
         }
     }
}
