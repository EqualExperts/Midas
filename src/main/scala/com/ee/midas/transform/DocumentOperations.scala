/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.transform

import org.bson.{BasicBSONObject, BasicBSONEncoder, BSONObject}
import scala.collection.JavaConverters._
import java.io.InputStream
import com.mongodb.{DefaultDBDecoder}
import com.ee.midas.utils.Loggable
import com.mongodb.util.JSON
import java.util.regex.{Matcher, Pattern}
import scala.language.postfixOps
import scala.language.implicitConversions

class DocumentOperations private (document: BSONObject) extends Loggable {

  private def isFieldADocument(fieldName: String, document: BSONObject) =
    document.get(fieldName).isInstanceOf[BSONObject]

  final def + [T] (field: String, value: T, overrideOldValue: Boolean = true): BSONObject = {
    logDebug("Adding/Updating Field %s with Value %s on Document %s".format(field, value.toString, document))
    apply(field) match {
      case Some(fieldValue) =>
        if(overrideOldValue) {
          update(field, value)
        } else {
          document
        }
      case None => update(field, value)
    }

    logDebug("After Adding/Updating Field %s on Document %s\n".format(field, document))
    document
  }

  final def - (name: String): BSONObject = {
    logDebug("Removing Field %s from Document %s".format(name, document))
    name.split("\\.") toList match {
      case topLevelField :: Nil => if(document.containsField(topLevelField)) document.removeField(topLevelField)

      case topLevelField :: rest => if(document.containsField(topLevelField)){
                            val nestedDocument = DocumentOperations(document.get(topLevelField).asInstanceOf[BSONObject])
                            nestedDocument - rest.mkString(".")
      }
    }
    logDebug("After Removing Field %s from Document %s\n".format(name, document))
    document
  }

  final def ++ (fields: BSONObject, overrideOldValues: Boolean = true) : BSONObject = {
    logDebug("Adding Fields %s to Document %s".format(fields, document))
    fields.keySet().asScala.foreach { field =>
      val defaultFieldValue = fields.get(field)
      DocumentOperations(document) + (field, defaultFieldValue, overrideOldValues)
    }
    logDebug("After Adding Fields to Document %s\n".format(document))
    document
  }
  
  final def -- (fields: BSONObject) : BSONObject = {
    logDebug("Removing Fields %s from Document %s".format(fields, document))
    fields.toMap.asScala.foreach { case(index, field) =>
      val fieldName = field.asInstanceOf[String]
      DocumentOperations(document) - fieldName
    }
    logDebug("After Removing Fields from Document %s\n".format(document))
    document
  }

  //split
  final def <~> (splitField: String, regex: Pattern, targetJsonDocument: String, overrideOldValue: Boolean = true) : BSONObject = {
    logDebug("Splitting Field %s in Document %s using Pattern %s".format(splitField, document, regex))
    apply(splitField) match {
      case Some(fieldValue) =>
        logDebug("and the value is: " + fieldValue)
        val matcher: Matcher = regex.matcher(fieldValue.asInstanceOf[String])
        var filledTargetJson = targetJsonDocument
        if(matcher.find) {
          val count = matcher.groupCount()
          (1 to count).foreach { token =>
            val groupValue = matcher.group(token)
            val replace = "$" + token
            filledTargetJson = filledTargetJson.replaceAllLiterally(replace, groupValue)
            logDebug(s"Replaced target document: $filledTargetJson" )
          }
          filledTargetJson = filledTargetJson.replaceAll("\\$[0-9]+", "")
          val splitDocument = JSON.parse(filledTargetJson).asInstanceOf[BSONObject]
          DocumentOperations(document) ++ (splitDocument, overrideOldValue)
          logDebug("After Splitting Fields in Document %s\n".format(document))
        } else {
          logDebug("Pattern %s Not applicable to Split Field (%s) having Data %s".format(regex, splitField, fieldValue))
        }
        document
      case None =>
        logDebug("Did not Split Field %s as Document does not have one".format(splitField))
        document
    }
  }

  //merge
  final def >~< (mergeField: String, usingSeparator: String, fields: List[String]) : BSONObject = {
    logDebug("Merging Fields %s in Document %s".format(fields, document))
    val fieldValues = (fields map apply) collect {
      case fieldValue: Option[Any] if(!fieldValue.isEmpty) => fieldValue.get
    }

    val mergedValue = fieldValues mkString usingSeparator
    DocumentOperations(document) + (mergeField, mergedValue)
    logDebug("After Merging Fields in Document %s\n".format(document))
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
          apply(topLevelField)
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
  private val ENCODER = new BasicBSONEncoder
  private val DECODER = new DefaultDBDecoder
  implicit def apply(document: BSONObject) = new DocumentOperations(document)
  implicit def unapply(in: InputStream) : BSONObject = DECODER.decode(in, null)
  implicit def unapply(bytes: Array[Byte]) : BSONObject = DECODER.decode(bytes, null)
}
