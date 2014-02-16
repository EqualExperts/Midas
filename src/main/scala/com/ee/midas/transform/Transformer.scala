package com.ee.midas.transform

import org.bson.BSONObject
import com.ee.midas.hotdeploy.{DeployableHolder}
import java.net.InetAddress
import com.ee.midas.config.{ChangeSet, Application}

//class Transformer(transformsHolder: DeployableHolder[Transforms], private var application: Application = Application("NOAPP", TransformType.EXPANSION, Nil)) {
class Transformer(private var transforms: Transforms, private var application: Application = Application("NOAPP", TransformType.EXPANSION, Nil)) {

//  def transforms: Transforms = transformsHolder.get

  def getApplication =
    this.synchronized {
      application
    }

  def getTransforms =
    this.synchronized {
      transforms
    }

  def updateApplication(newApplication: Application) =
    this.synchronized {
      application = newApplication
    }

  def updateTransforms(newTransforms: Transforms) =
    this.synchronized {
      transforms = newTransforms
    }

  def transformResponse(document: BSONObject, fullCollectionName: String): BSONObject = {
    getTransforms.transformResponse(document, fullCollectionName)
  }

  def transformRequest(document: BSONObject, fullCollectionName: String, ip: InetAddress): BSONObject = {
    getApplication.changeSet(ip) match {
      case Some(ChangeSet(cs)) => transforms.transformRequest(document, cs, fullCollectionName)
      case None => document
    }
  }
}
