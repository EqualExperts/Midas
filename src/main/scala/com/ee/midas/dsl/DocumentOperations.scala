package com.ee.midas.dsl

import org.bson.BSONObject
import scala.collection.JavaConverters._

class DocumentOperations private (document: BSONObject) {
  def + (fields: BSONObject) : BSONObject = {
    println("Adding Fields %s to Document %s".format(fields, document))
    document.putAll(fields)
    println("After Adding Fields to Document %s\n".format(document))
    document
  }
  
  def - (fields: BSONObject) : BSONObject = {
    println("Removing Fields %s from Document %s".format(fields, document))
    fields.toMap.asScala.foreach { case(index, value) =>
      val name = value.asInstanceOf[String]
      document.removeField(name)
    }
    println("After Removing Fields from Document %s\n".format(document))
    document
  }

  //split
  def <~> (fields: BSONObject) : BSONObject = {
    document
  }

  //merge
  def >~< (fields: BSONObject) : BSONObject = {
    document
  }
}

object DocumentOperations {
  implicit def apply(document: BSONObject) = new DocumentOperations(document)
}
