package com.ee.midas.transform

import org.bson.{BasicBSONEncoder, BSONObject}
import scala.collection.JavaConverters._
import java.io.InputStream
import com.mongodb.{DefaultDBDecoder, DBDecoder}
import com.ee.midas.utils.Loggable

class DocumentOperations private (document: BSONObject) extends Loggable {
  def + [T] (field: String, value: T): BSONObject = {
    log.info("Adding/Updating Field %s with Value %s on Document %s".format(field, value.toString, document))
    document.put(field, value)
    log.info("After Adding/Updating Field %s on Document %s\n".format(field, document))
    document
  }

  def - (name: String): BSONObject = {
    log.info("Removing Field %s from Document %s".format(name, document))
    document.removeField(name)
    log.info("After Removing Field %s from Document %s\n".format(name, document))
    document
  }

  def ++ (fields: BSONObject) : BSONObject = {
    log.info("Adding Fields %s to Document %s".format(fields, document))
    document.putAll(fields)
    log.info("After Adding Fields to Document %s\n".format(document))
    document
  }
  
  def -- (fields: BSONObject) : BSONObject = {
    log.info("Removing Fields %s from Document %s".format(fields, document))
    fields.toMap.asScala.foreach { case(index, value) =>
      val name = value.asInstanceOf[String]
      document.removeField(name)
    }
    log.info("After Removing Fields from Document %s\n".format(document))
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
  private val DECODER = new DefaultDBDecoder()
  implicit def apply(document: BSONObject) = new DocumentOperations(document)
  implicit def unapply(in: InputStream) : BSONObject = DECODER.decode(in, null)
}
