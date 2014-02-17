package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.bson.BasicBSONObject
import com.ee.midas.transform.TransformType._
import com.ee.midas.config.Application

@RunWith(classOf[JUnitRunner])
class TransformerSpecs extends Specification with Mockito {

    "Transformer" should {
       "Get Application" in {
          //Given
          val application = Application("MyApp", EXPANSION, Nil)
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
         val transformer = new Transformer(transforms)

         //When
         val actualTransforms = transformer.getTransforms

         //Then
         actualTransforms mustEqual transforms
       }

       "Update Application" in {
         //Given
         val application = Application("MyApp", EXPANSION, Nil)
         val transforms = mock[Transforms]
         val transformer = new Transformer(transforms, application)
         val newApplication = Application("NewApp", EXPANSION, Nil)

         //When
         transformer.updateApplication(newApplication)
         val updatedApplication = transformer.getApplication

         //Then
         updatedApplication mustEqual newApplication
       }

      "Update Transforms" in {
        //Given
        val transforms = mock[Transforms]
        val transformer = new Transformer(transforms)
        val newTransforms = mock[Transforms]

        //When
        transformer.updateTransforms(newTransforms)
        val actualTransforms = transformer.getTransforms

        //Then
        actualTransforms mustEqual newTransforms
      }

     "transform document in EXPANSION mode" in {
      //Given
      val transforms = mock[Transforms]
      val fullCollectionName : String = "name"
      val document = new BasicBSONObject("name","testCollection")
      val expectedDocument = document.append("new", "value")
      transforms.transformResponse(document, fullCollectionName) returns expectedDocument

      //When
      val transformer = new Transformer(transforms)

      //Then
      transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
      there was one(transforms).transformResponse(document, fullCollectionName)
    }

    "transforms document in CONTRACTION mode" in {
      //Given
      val transforms = mock[Transforms]
      val fullCollectionName : String = "name"
      val document = new BasicBSONObject("name","testCollection")
      val expectedDocument = document.append("new","value")
      transforms.transformResponse(document, fullCollectionName) returns expectedDocument

      //When
      val transformer = new Transformer(transforms)

      //Then
      transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
      there was one(transforms).transformResponse(document, fullCollectionName)
    }
    }

}
