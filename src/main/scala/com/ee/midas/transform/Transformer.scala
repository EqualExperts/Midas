package com.ee.midas.transform

import org.bson.BSONObject
import java.net.InetAddress
import com.ee.midas.config.{ChangeSet, Application}

class Transformer(private var transforms: Transforms, private var application: Application) {

  def getApplication =
    this.synchronized {
      application
    }

  def getTransforms =
    this.synchronized {
      transforms
    }

  def update(newApplication: Application, newTransforms: Transforms) =
    this.synchronized {
      application = newApplication
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

  override def toString = s"""Transformer for ${getApplication.name} => ${getTransforms}"""
}
