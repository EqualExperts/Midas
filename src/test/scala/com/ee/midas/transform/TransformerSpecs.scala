package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import com.ee.midas.hotdeploy.DeployableHolder
import org.bson.BasicBSONObject

@RunWith(classOf[JUnitRunner])
class TransformerSpecs extends Specification with Mockito {
      "Transformer" should {
         "check if document can be transformed in EXPANSION mode" in {
             val deployableHolder =  mock[DeployableHolder[Transforms]]
             val transforms = mock[Transforms]
             val fullCollectionName = "testCollection"
             deployableHolder.get returns transforms
             transforms.canBeApplied(fullCollectionName) returns true

             val transformer = new Transformer(TransformType.EXPANSION , deployableHolder)

             transformer.canTransformDocuments(fullCollectionName)  mustEqual true
             there was one(transforms).canBeApplied(fullCollectionName)
         }

        "check if document can be transformed in CONTRACTION mode" in {
          val deployableHolder =  mock[DeployableHolder[Transforms]]
          val transforms = mock[Transforms]
          deployableHolder.get returns transforms
          val fullCollectionName = "testCollection"
          transforms.canBeApplied(fullCollectionName) returns true

          val transformer = new Transformer(TransformType.CONTRACTION , deployableHolder)

          transformer.canTransformDocuments(fullCollectionName) mustEqual true
          there was one(transforms).canBeApplied(fullCollectionName)
        }

        "transforms document in EXPANSION mode" in {
          val deployableHolder =  mock[DeployableHolder[Transforms]]
          val transforms = mock[Transforms]
          val fullCollectionName : String = "name"
          val transformType = TransformType.EXPANSION
          deployableHolder.get returns transforms
          val document = new BasicBSONObject("name","testCollection")
          val transformedDocument = document.append("new","value")
          transforms.map(document)(fullCollectionName, transformType) returns transformedDocument

          val transformer = new Transformer(TransformType.EXPANSION, deployableHolder)

          transformer.transform(document)(fullCollectionName)  mustEqual  transformedDocument
          there was one(transforms).map(document)(fullCollectionName, transformType)
        }

        "transforms document in CONTRACTION mode" in {
          val deployableHolder =  mock[DeployableHolder[Transforms]]
          val transforms = mock[Transforms]
          val fullCollectionName : String = "name"
          val transformType = TransformType.CONTRACTION
          deployableHolder.get returns transforms
          val document = new BasicBSONObject("name","testCollection")
          val transformedDocument = document.append("new","value")
          transforms.map(document)(fullCollectionName, transformType) returns transformedDocument

          val transformer = new Transformer(TransformType.CONTRACTION, deployableHolder)

          transformer.transform(document)(fullCollectionName)  mustEqual  transformedDocument
          there was one(transforms).map(document)(fullCollectionName, transformType)
        }
      }
}
