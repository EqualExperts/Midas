package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import com.ee.midas.hotdeploy.DeployableHolder
import org.bson.BasicBSONObject

@RunWith(classOf[JUnitRunner])
class TransformerSpecs extends Specification with Mockito {
 /*
      "Transformer" should {
         "transform document in EXPANSION mode" in {
          //Given
          val deployableHolder =  mock[DeployableHolder[Transforms]]
          val transforms = mock[Transforms]
          val fullCollectionName : String = "name"
          deployableHolder.get returns transforms
          val document = new BasicBSONObject("name","testCollection")
          val expectedDocument = document.append("new","value")
          transforms.transformResponse(document, fullCollectionName) returns expectedDocument

          //When
          val transformer = new Transformer(deployableHolder)

          //Then
          transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
          there was one(transforms).transformResponse(document, fullCollectionName)
        }

        "transforms document in CONTRACTION mode" in {
          //Given
          val deployableHolder =  mock[DeployableHolder[Transforms]]
          val transforms = mock[Transforms]
          val fullCollectionName : String = "name"
          deployableHolder.get returns transforms
          val document = new BasicBSONObject("name","testCollection")
          val expectedDocument = document.append("new","value")
          transforms.transformResponse(document, fullCollectionName) returns expectedDocument

          //When
          val transformer = new Transformer(deployableHolder)

          //Then
          transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
          there was one(transforms).transformResponse(document, fullCollectionName)
        }
      }
      */
}
