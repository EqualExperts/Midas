package com.ee.midas.transform

import org.bson.{BasicBSONEncoder, BSONObject}
import scala.collection.JavaConverters._

class DocumentOperations private (document: BSONObject) {
  def + [T] (field: String, value: T): BSONObject = {
    println("Adding/Updating Field %s with Value %s on Document %s".format(field, value.toString, document))
    document.put(field, value)
    println("After Adding/Updating Field %s on Document %s\n".format(field, document))
    document
  }

  def - (name: String): BSONObject = {
    println("Removing Field %s from Document %s".format(name, document))
    document.removeField(name)
    println("After Removing Field %s from Document %s\n".format(name, document))
    document
  }

  def ++ (fields: BSONObject) : BSONObject = {
    println("Adding Fields %s to Document %s".format(fields, document))
    document.putAll(fields)
    println("After Adding Fields to Document %s\n".format(document))
    document
  }
  
  def -- (fields: BSONObject) : BSONObject = {
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

  def toBytes: Array[Byte] = DocumentOperations.ENCODER.encode(document)
}

object DocumentOperations {
  private val ENCODER = new BasicBSONEncoder()
  implicit def apply(document: BSONObject) = new DocumentOperations(document)
}
