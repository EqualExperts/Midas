package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.bson.BasicBSONObject
import com.ee.midas.transform.TransformType._
import com.ee.midas.config.Application
import java.net.URL

@RunWith(classOf[JUnitRunner])
class TransformerSpecs extends Specification with Mockito {

    "Transformer" should {
       "Get Application" in {
          //Given
          val appConfigDir = new URL("file:/myApp")
          val application = Application(appConfigDir, "MyApp", EXPANSION, Nil)
          val transforms = mock[Transforms]
          val transformer = new Transformer(transforms, application)

          //When
          val actualApplication = transformer.getApplication

          //Then
          actualApplication mustEqual application
       }

       "Get Transforms" in {
         //Given
         val transforms = mock[Transforms]
         val ignoreConfigDir: URL = null
         val application = Application(ignoreConfigDir, "someApp", EXPANSION, Nil)
         val transformer = new Transformer(transforms, application)

         //When
         val actualTransforms = transformer.getTransforms

         //Then
         actualTransforms mustEqual transforms
       }

       "Update Application and Transforms" in {
         //Given
         val appConfigDir = new URL("file:/myApp")
         val application = Application(appConfigDir, "MyApp", EXPANSION, Nil)
         val transforms = mock[Transforms]
         val transformer = new Transformer(transforms, application)
         val newApplication = Application(appConfigDir, "MyAppVer2_1_1", EXPANSION, Nil)
         val newTransforms = mock[Transforms]

         //When
         transformer.update(newApplication, newTransforms)

         //Then
         transformer.getTransforms  mustEqual newTransforms
         transformer.getApplication mustEqual newApplication
       }

     "transform document in EXPANSION mode" in {
      //Given
      val transforms = mock[Transforms]
      val ignoreConfigDir: URL = null
      val ignoreApplication = Application(ignoreConfigDir, "someApp", EXPANSION, Nil)

      val fullCollectionName : String = "name"
      val document = new BasicBSONObject("name","testCollection")
      val expectedDocument = document.append("new", "value")
      transforms.transformResponse(document, fullCollectionName) returns expectedDocument

      //When
      val transformer = new Transformer(transforms, ignoreApplication)

      //Then
      transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
      there was one(transforms).transformResponse(document, fullCollectionName)
    }

    "transforms document in CONTRACTION mode" in {
      //Given
      val transforms = mock[Transforms]
      val ignoreConfigDir: URL = null
      val ignoreApplication = Application(ignoreConfigDir, "someApp", EXPANSION, Nil)
      val fullCollectionName : String = "name"
      val document = new BasicBSONObject("name","testCollection")
      val expectedDocument = document.append("new","value")
      transforms.transformResponse(document, fullCollectionName) returns expectedDocument

      //When
      val transformer = new Transformer(transforms, ignoreApplication)

      //Then
      transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
      there was one(transforms).transformResponse(document, fullCollectionName)
    }
    }

}
