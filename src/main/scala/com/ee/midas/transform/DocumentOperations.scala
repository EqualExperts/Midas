package com.ee.midas.transform

import org.bson.{BasicBSONObject, BasicBSONEncoder, BSONObject}
import scala.collection.JavaConverters._
import java.io.InputStream
import com.mongodb.{DefaultDBDecoder}
import com.ee.midas.utils.Loggable
import com.mongodb.util.JSON

class DocumentOperations private (document: BSONObject) extends Loggable {
  def + [T] (field: String, value: T): BSONObject = {
    log.debug("Adding/Updating Field %s with Value %s on Document %s".format(field, value.toString, document))
    document.put(field, value)
    log.debug("After Adding/Updating Field %s on Document %s\n".format(field, document))
    document
  }

  def - (name: String): BSONObject = {
    log.debug("Removing Field %s from Document %s".format(name, document))
    document.removeField(name)
    log.debug("After Removing Field %s from Document %s\n".format(name, document))
    document
  }

  def ++ (fields: BSONObject) : BSONObject = {
    log.debug("Adding Fields %s to Document %s".format(fields, document))
    val keys = fields.keySet().asScala
    val (nestedKeys, normalKeys) = keys.partition(key => key.contains("."))
    if(nestedKeys.isEmpty){
      normalKeys.filter (!document.containsField(_)) foreach { key =>
        document.put(key, fields.get(key))
      }
    } else {
      normalKeys.foreach(key => document.put(key, fields.get(key)))
      nestedKeys.foreach { key => {
          val currentKey = key.takeWhile(_!='.')
          if(document.containsField(currentKey))
            DocumentOperations(document.get(currentKey).asInstanceOf[BasicBSONObject]) ++ new BasicBSONObject(key.dropWhile(_!='.').tail, fields.get(key))
          else
            document.put(currentKey, DocumentOperations(new BasicBSONObject()) ++ new BasicBSONObject(key.dropWhile(_!='.').tail, fields.get(key)))
        }
      }
    }

    log.debug("After Adding Fields to Document %s\n".format(document))
    document
  }
  
  def -- (fields: BSONObject) : BSONObject = {
    log.debug("Removing Fields %s from Document %s".format(fields, document))
    fields.toMap.asScala.foreach { case(index, field) =>
      val fieldName = field.asInstanceOf[String]
      if(fieldName.contains(".")) {
        val currentKey = fieldName.takeWhile(_ != '.')
        if (document.containsField(currentKey)) {
          val nextLevelDocument = document.get(currentKey).asInstanceOf[BSONObject]
          val remainingLevels = s"""["${fieldName.dropWhile(_ != '.').tail}"]"""
          val fieldsToRemove = JSON.parse(remainingLevels).asInstanceOf[BSONObject] 
          DocumentOperations(nextLevelDocument) -- fieldsToRemove
        }
      }
      else
        document.removeField(fieldName)
    }
    log.debug("After Removing Fields from Document %s\n".format(document))
    document
  }

  //split
  def <~> (fields: BSONObject) : BSONObject = {
    document
  }

  //merge
  def >~< (mergeFieldName: String, usingSeparator: String, fields: BSONObject) : BSONObject = {
    log.debug("Merging Fields %s in Document %s".format(fields, document))
    val fieldValues = fields.toMap.asScala.map { case (index, field) =>
      val fieldName = field.asInstanceOf[String]
      if(document.containsField(fieldName)) {
        document.get(fieldName).toString
      }
    }
    val nonEmptyFieldValues = fieldValues.filter(_.isInstanceOf[String])
    val mergedValues = nonEmptyFieldValues mkString usingSeparator
    DocumentOperations(document) + (mergeFieldName, mergedValues)
    log.debug("After Merging Fields in Document %s\n".format(document))
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
