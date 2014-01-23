package com.ee.midas.transform

import org.bson.{BasicBSONObject, BasicBSONEncoder, BSONObject}
import scala.collection.JavaConverters._
import java.io.InputStream
import com.mongodb.{BasicDBList, DefaultDBDecoder}
import com.ee.midas.utils.Loggable
import com.mongodb.util.JSON
import java.util.regex.{Matcher, Pattern}
import com.ee.midas.dsl.expressions.{Literal, Expression}
import org.bson.types.BasicBSONList

class DocumentOperations private (document: BSONObject) extends Loggable {

  private def isFieldADocument(fieldName: String, document: BSONObject) =
    document.get(fieldName).isInstanceOf[BSONObject]

  final def + [T] (field: String, value: T, overrideOldValue: Boolean = true): BSONObject = {
    log.debug("Adding/Updating Field %s with Value %s on Document %s".format(field, value.toString, document))
    apply(field) match {
      case Some(fieldValue) =>
        if(overrideOldValue) {
          update(field, value)
        } else {
          document
        }
      case None => update(field, value)
    }

    log.debug("After Adding/Updating Field %s on Document %s\n".format(field, document))
    document
  }

  final def - (name: String): BSONObject = {
    log.debug("Removing Field %s from Document %s".format(name, document))
    name.split("\\.") toList match {
      case topLevelField :: Nil => if(document.containsField(topLevelField)) document.removeField(topLevelField)

      case topLevelField :: rest => if(document.containsField(topLevelField)){
                            val nestedDocument = DocumentOperations(document.get(topLevelField).asInstanceOf[BSONObject])
                            nestedDocument - rest.mkString(".")
      }
    }
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
      DocumentOperations(document) - fieldName
    }
    log.debug("After Removing Fields from Document %s\n".format(document))
    document
  }

  //split
  final def <~> (splitField: String, regex: Pattern, targetJsonDocument: String, overrideOldValue: Boolean = true) : BSONObject = {
    log.debug("Splitting Field %s in Document %s using Pattern %s".format(splitField, document, regex))
    apply(splitField) match {
      case Some(fieldValue) =>
        log.debug("and the value is: " + fieldValue)
        val matcher: Matcher = regex.matcher(fieldValue.asInstanceOf[String])
        var filledTargetJson = targetJsonDocument
        if(matcher.find) {
          val count = matcher.groupCount()
          (1 to count).foreach { token =>
            val groupValue = matcher.group(token)
            val replace = "$" + token
            filledTargetJson = filledTargetJson.replaceAllLiterally(replace, groupValue)
            log.debug(s"Replaced target document: $filledTargetJson" )
          }
          val splitDocument = JSON.parse(filledTargetJson).asInstanceOf[BSONObject]
          DocumentOperations(document) ++ (splitDocument, overrideOldValue)
          log.debug("After Splitting Fields in Document %s\n".format(document))
        } else {
          log.debug("Pattern %s Not applicable to Split Field (%s) having Data %s".format(regex, splitField, fieldValue))
        }
        document
      case None =>
        log.debug("Did not Split Field %s as Document does not have one".format(splitField))
        document
    }
  }

  //merge
  final def >~< (mergeField: String, usingSeparator: String, fields: List[String]) : BSONObject = {
    log.debug("Merging Fields %s in Document %s".format(fields, document))
    val fieldValues = (fields map apply) collect {
      case fieldValue: Option[Any] if(!fieldValue.isEmpty) => fieldValue.get
    }

    val mergedValue = fieldValues mkString usingSeparator
    DocumentOperations(document) + (mergeField, mergedValue)
    log.debug("After Merging Fields in Document %s\n".format(document))
    document
  }

  final def toBytes: Array[Byte] = DocumentOperations.ENCODER.encode(document)

  final def apply(fieldName: String): Option[Any] = {
    fieldName.split("\\.") toList match {
      case topLevelField :: Nil => if (document.containsField(fieldName)) Some(document.get(fieldName)) else None
      case topLevelField :: rest =>
        if(isFieldADocument(topLevelField, document)) {
          val nestedDocument = document.get(topLevelField).asInstanceOf[BSONObject]
          readNestedValue(rest, nestedDocument)
        } else {
          Some(document.get(topLevelField))
        }
    }
  }

  private def readNestedValue(fields: List[String], document: BSONObject): Option[AnyRef] = {
    if(document.containsField(fields.head)) {
      document.get(fields.head) match {
        case (value: BSONObject) => readNestedValue(fields.tail, value)
        case v => Some(v)
      }
    } else {
      None
    }
  }

  private def writeNestedValue(fieldNames: List[String], document: BSONObject, value: Any): AnyRef = {
    fieldNames match {
      case field :: Nil => document.put(field, value)
      case topLevelField :: rest =>
        if(!isFieldADocument(topLevelField, document)) {
          document.put(topLevelField, new BasicBSONObject())
        }
        val nestedDocument = document.get(topLevelField).asInstanceOf[BSONObject]
        writeNestedValue(rest, nestedDocument, value)
    }
    document
  }

  final def update(fieldName: String, value: Any) = {
    fieldName.split("\\.") toList match {
      case topLevelField :: Nil => document.put(topLevelField, value)
      case topLevelField :: rest =>
      if(!isFieldADocument(topLevelField, document)) {
        document.put(topLevelField, new BasicBSONObject())
      }
      val nestedDocument = document.get(topLevelField).asInstanceOf[BSONObject]
      writeNestedValue(rest, nestedDocument, value)
    }
    document
  }
}

object DocumentOperations {
  private val ENCODER = new BasicBSONEncoder()
  private val DECODER = new DefaultDBDecoder()
  implicit def apply(document: BSONObject) = new DocumentOperations(document)
  implicit def unapply(in: InputStream) : BSONObject = DECODER.decode(in, null)
}
