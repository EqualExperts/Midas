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

package com.ee.midas.fixtures

import com.mongodb._
import org.specs2.form.Form
import org.specs2.specification.Forms._
import org.bson.BSONObject
import java.util.ArrayList
import com.ee.midas.transform.DocumentOperations._

case class MongoShell(formName: String, host: String, port: Int) extends FormBuilder {
  var mongoClient: MongoClient = new MongoClient(host, port)
  var db: DB = null
  var shell = Form(formName)
  var documents: ArrayList[DBObject] = null

  def close = mongoClient.close()

  def useDatabase(dbName: String) = {
    db = mongoClient.getDB(dbName)
    shell = shell.tr(field(s">use $dbName"))
    this
  }

  def runCommand(query: String) = {
    try{
       val result = db.doEval(query)
       shell = shell.tr(prop(s">$query", result.ok(), true))
    } catch {
       case e: Exception => shell = shell.tr("Client cannot Connect")
    }
    this
  }

  def readDocumentsFromCollection(collectionName: String) = {
    documents = new ArrayList[DBObject]()
    val cursor = db.getCollection(collectionName).find()
    while(cursor.hasNext) {
      val document = cursor.next()
      documents.add(document)
    }
    this
  }

  def update(collection: String, findQuery: DBObject, updateDocument: DBObject) = {
     val result: WriteResult = db.getCollection(collection).update(findQuery, updateDocument)
     val query = collection + ".update(" + findQuery + ", " + updateDocument + ")"
     shell = shell.tr(prop(query, result.get.getField("updatedExisting"), true))
     this
  }

  def insert(collection: String, insertDocument: DBObject) = {
     val result = db.getCollection(collection).insert(insertDocument)
     val query = collection + ".insert(" + insertDocument + ")"
     shell = shell.tr(prop(query, result.get.getField("ok"), 1.0))
     this
  }

  def assertAllDocumentsHaveEqualValuesInNewAndOldFields(newOldFields: Array[(String, String)], expansionVersion: Int) = {
     documents.toArray.foreach({ document =>
       shell = shell.tr(field(s"document", document))
       for(newOldField <- newOldFields)
         assertEqualValuesInNewAndOldFields(document.asInstanceOf[DBObject], newOldField)
       assertExpansionVersion(document.asInstanceOf[DBObject], expansionVersion)
     })
     this
  }

  private def assertEqualValuesInNewAndOldFields(document: DBObject, newAndOldField: (String, String)) = {
     val oldField = newAndOldField._1
     val newField = newAndOldField._2
     shell = shell.tr(prop(s"document(${newField})", document(oldField), document(newField)))
  }

  def assertAllDocumentsHaveExpanded(expansionVersion: Int) = {
    documents.toArray.foreach({ document =>
      shell = shell.tr(field(s"document", document))
      assertExpansionVersion(document.asInstanceOf[DBObject], expansionVersion)
    })
    this
  }

  def assertAllDocumentsHaveContracted(contractionVersion: Int) = {
    documents.toArray.foreach({ document =>
      shell = shell.tr(field(s"document", document))
      assertContractionVersion(document.asInstanceOf[DBObject], contractionVersion)
    })
    this
  }

  def assertExpansionVersion(document:DBObject, expansionVersion: Int) = {
     val expansionVersion = document.asInstanceOf[DBObject].get("_expansionVersion")
     shell = shell.tr(prop("document('_expansionVersion')", expansionVersion, expansionVersion))
  }

  def assertFieldsRemoved (fields: Array[String], contractionVersion: Int) = {
    documents.toArray.foreach({ doc =>
      val document = doc.asInstanceOf[DBObject]
      shell = shell.tr(field(s"document", document))
      for (field <- fields)
        shell = shell.tr(prop(s"!document.containsField($field)", !document.containsField(field), true))
        assertContractionVersion(document, contractionVersion)
    })
    this
  }

  def assertFieldsAdded(fields: Array[String], expansionVersion: Int) = {
    documents.toArray.foreach({ doc =>
      val document = doc.asInstanceOf[DBObject]
      shell = shell.tr(field(s"document", document))
      for(field <- fields)
        shell = shell.tr(prop(s"document.containsField($field)", document.containsField(field), true))
        assertExpansionVersion(document, expansionVersion)
    })
    this
  }

  def assertContractionVersion(document:DBObject, contractionVersion: Int) = {
    val contractionVersion = document.asInstanceOf[DBObject].get("_contractionVersion")
    shell = shell.tr(prop("document('_contractionVersion')", contractionVersion, contractionVersion))
  }

  def build = {
    close
    shell
  }
}
