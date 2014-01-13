package com.ee.midas.transform

import org.bson.{BasicBSONObject, BasicBSONEncoder, BSONObject}
import scala.collection.JavaConverters._
import java.io.InputStream
import com.mongodb.{DefaultDBDecoder}
import com.ee.midas.utils.Loggable
import com.mongodb.util.JSON
import java.util.regex.{Matcher, Pattern}

class DocumentOperations private (document: BSONObject) extends Loggable {

  private def isFieldADocument(fieldName: String, document: BSONObject) =
    document.get(fieldName).isInstanceOf[BSONObject]

  final def + [T] (field: String, value: T, overrideOldValue: Boolean = true): BSONObject = {
    log.debug("Adding/Updating Field %s with Value %s on Document %s".format(field, value.toString, document))
    field.split("\\.").toList match {
      case topLevelField :: Nil =>
        if(overrideOldValue) {
          document.put(topLevelField, value)
        }
      case topLevelField :: rest =>
        if(isFieldADocument(topLevelField, document)) {
          log.debug("After Adding/Updating Field %s on Document %s\n".format(field, document))
          val innerDocument = document.get(topLevelField).asInstanceOf[BSONObject]
          val remaining = rest mkString "."
          DocumentOperations(innerDocument) + (remaining, value, overrideOldValue)
        } else {
          val innerEmptyDocument = new BasicBSONObject()
          val remaining = rest mkString "."
          document.put(topLevelField, innerEmptyDocument)
          DocumentOperations(innerEmptyDocument) + (remaining, value, overrideOldValue)
        }
    }
    log.debug("After Adding/Updating Field %s on Document %s\n".format(field, document))
    document
  }

  final def - (name: String): BSONObject = {
    log.debug("Removing Field %s from Document %s".format(name, document))
    document.removeField(name)
    log.debug("After Removing Field %s from Document %s\n".format(name, document))
    document
  }

  final def ++ (fields: BSONObject, overrideOldValues: Boolean = true) : BSONObject = {
    log.debug("Adding Fields %s to Document %s".format(fields, document))
    fields.keySet().asScala.foreach { field =>
      val defaultFieldValue = fields.get(field)
      DocumentOperations(document) + (field, defaultFieldValue, overrideOldValues)
    }
    log.debug("After Adding Fields to Document %s\n".format(document))
    document
  }
  
  final def -- (fields: BSONObject) : BSONObject = {
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
  final def <~> (splitField: String, regex: Pattern, targetJsonDocument: String) : BSONObject = {
    if(document.containsField(splitField)) {
      log.debug("Splitting Field %s in Document %s using Pattern %s".format(splitField, document, regex))
      val splitFieldValue = document.get(splitField).asInstanceOf[String]
      val matcher: Matcher = regex.matcher(splitFieldValue)
      var filledTargetJson = targetJsonDocument
      if(matcher.find) {
        val count = matcher.groupCount()
        (1 to count).foreach { i =>
          val groupValue = matcher.group(i)
          val replace = "$" + i
          filledTargetJson = filledTargetJson.replaceAllLiterally(replace, groupValue)
          log.debug(s"Replaced target document: $filledTargetJson" )
        }
        val splitDocument = JSON.parse(filledTargetJson).asInstanceOf[BSONObject]
        DocumentOperations(document) ++ splitDocument
        log.debug("After Splitting Fields in Document %s\n".format(document))
      } else {
        log.debug("Pattern %s Not applicable to Split Field (%s) having Data %s".format(regex, splitField, splitFieldValue))
      }
    } else {
      log.debug("Did not Split Field %s as Document does not have one".format(splitField))
      document
    }
    document
  }

  //merge
  final def >~< (mergeField: String, usingSeparator: String, fields: List[String]) : BSONObject = {
    log.debug("Merging Fields %s in Document %s".format(fields, document))
    val fieldValues = fields filter document.containsField map document.get
    val mergeValue = fieldValues mkString usingSeparator
    DocumentOperations(document) + (mergeField, mergeValue)
    log.debug("After Merging Fields in Document %s\n".format(document))
    document
  }

  final def toBytes: Array[Byte] = DocumentOperations.ENCODER.encode(document)
}

object DocumentOperations {
  private val ENCODER = new BasicBSONEncoder()
  private val DECODER = new DefaultDBDecoder()
  implicit def apply(document: BSONObject) = new DocumentOperations(document)
  implicit def unapply(in: InputStream) : BSONObject = DECODER.decode(in, null)
}
