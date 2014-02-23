package com.ee.midas

import org.specs2.mutable.{BeforeAfter, Specification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.{Transformer}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.{ScalaGenerator}
import java.io.{PrintWriter, File}
import org.specs2.mock.Mockito
import com.ee.midas.transform.TransformType._
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class DeltasProcessorSpecs extends Specification with Mockito with DeltasProcessor {
     trait SetupTeardown extends BeforeAfter {
       val myDeltas = new File("src/test/scala/com/ee/midas/myDeltas")
       val myApp = new File(myDeltas.getAbsolutePath + "/myApp")
       val changeSet01 = new File(myApp.getAbsolutePath + "/001-ChangeSet")
       val expansion = new File(changeSet01.getAbsolutePath + "/expansion")
       val contraction = new File(changeSet01.getAbsolutePath + "/contraction")
       val deltasDirURL =  myDeltas.toURI.toURL
       val expansionDeltaFile = new File(expansion.getPath + "/01-expansion.delta")
       val contractionDeltaFile = new File(contraction.getPath + "/01contraction.delta")

       def before: Any = {
         expansion.mkdirs()
         contraction.mkdirs()
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
         "process response expansion delta files " in new SetupTeardown {
           //Given
           val translator = new Translator[Transformer](new Reader, new ScalaGenerator)

           //When
           val transformer = processDeltas(translator, EXPANSION, deltasDirURL).get

           //Then
           val expansions = transformer.responseExpansions
           expansions must haveLength(1)
           expansions must haveKey("someDatabase.collection")

           //And
           val contractions = transformer.responseContractions
           contractions must be empty
         }

         "process response contraction delta files " in new SetupTeardown {
           //Given
           val translator = new Translator[Transformer](new Reader, new ScalaGenerator)

           //When
           val transformer = processDeltas(translator, CONTRACTION, deltasDirURL).get

           //Then
           val contractions = transformer.responseContractions
           contractions must haveLength(1)
           contractions must haveKey("someDatabase.collection")

           //And
           val expansions = transformer.responseExpansions
           expansions must be empty
         }

         "process request expansion delta files " in new SetupTeardown {
           //Given
           val translator = new Translator[Transformer](new Reader, new ScalaGenerator)

           //When
           val transformer = processDeltas(translator, EXPANSION, deltasDirURL).get

           //Then
           val expansions = transformer.requestExpansions
           expansions must haveLength(1)
           expansions must haveKey((1, "someDatabase.collection"))

           //And
           val contractions = transformer.requestContractions
           contractions must be empty
         }

         "process request contraction delta files " in new SetupTeardown {
           //Given
           val translator = new Translator[Transformer](new Reader, new ScalaGenerator)

           //When
           val transformer = processDeltas(translator, CONTRACTION, deltasDirURL).get

           //Then
           val contractions = transformer.requestContractions
           contractions must haveLength(1)
           contractions must haveKey((1, "someDatabase.collection"))

           //And
           val expansions = transformer.requestExpansions
           expansions must haveLength(1)
           expansions must haveKey((1, "someDatabase.collection"))
         }

       //todo:
//         "Fails when the configDir url does not exist" in {
//
//         }
//
//         "Fails when the configDir url is empty" in {
//         }
//
//         "Fails when it cannot parse delta files" in { }
     }
}
