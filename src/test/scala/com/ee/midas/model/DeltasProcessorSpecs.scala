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

package com.ee.midas.model

import org.specs2.mutable.{BeforeAfter, Specification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.Transformer
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import java.io.{PrintWriter, File}
import org.specs2.mock.Mockito
import com.ee.midas.transform.TransformType._

@RunWith(classOf[JUnitRunner])
class DeltasProcessorSpecs extends Specification with Mockito with DeltasProcessor {
     trait SetupTeardown extends BeforeAfter {
       val myDeltas = new File("test-data/deltaProcessorSpecs")
       val myNewApp = new File(myDeltas.getAbsolutePath + "/myNewApp")
       val changeSet01 = new File(myNewApp.getAbsolutePath + "/001-ChangeSet")
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
     }
}
