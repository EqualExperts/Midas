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

package com.ee.midas.acceptance

import com.mongodb._
import org.junit.runner.RunWith
import org.specs2.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MidasWithJavaDriverSpecs extends Specification {

  var application: MongoClient = null
  var document:DBObject = null

  def is = sequential ^ s2"""
    Narration:
    //TODO: write a story to represent CRUD.
    This is a specification to verify that midas behaves as a proxy

    A client application should
        Step 1: Ensure Midas and mongods are running
            Connect to Midas                 $connect

        Step 2: Perform CRUD operations
            insert documents                 $insert
            read documents                   $read
            update documents                 $update
            delete documents                 $delete
            drop database                    $drop

        Step 3: Close connection to Midas
            Disconnect                       $disconnect
                                                               """

  def connect = {
    application = new MongoClient("localhost", 27020)
    application.getConnector.isOpen
  }

  def insert = {
    document = new BasicDBObject("testName","midas is a proxy")
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def result:WriteResult = collection.insert(document)
    result.getError == null
  }

  def read = {
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def readDocument:DBObject = collection.findOne()
    readDocument == document
  }

  def update = {
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def document = collection.findOne
    document.put("version", 1)
    def result:WriteResult = collection.update(collection.findOne, document)
    result.getError == null
  }

  def delete = {
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def result:WriteResult = collection.remove(document)
    result.getError == null
  }

  def drop = {
    def database:DB = application.getDB("midasSmokeTest")
    database.dropDatabase()
    true
  }


  def disconnect = {
    application.close()
    application.getConnector.isOpen must beFalse
  }
}
