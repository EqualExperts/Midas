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

package com.ee.midas.smoke

import com.mongodb._
import org.junit.runner.RunWith
import org.specs2.Specification
import org.specs2.runner.JUnitRunner
import com.ee.midas.fixtures.MidasUtils
import java.io.{PrintWriter, File}
import java.net.{URL, InetAddress}
import com.ee.midas.model._
import com.ee.midas.transform.TransformType

@RunWith(classOf[JUnitRunner])
class CrudSpecs extends Specification {

  var application: MongoClient = null
  var document:DBObject = null

  object DeltasSetup {
    val deltasDirPath = System.getProperty("user.dir") + File.separator + "/deltas"
    val deltasDir: File = new File(deltasDirPath)

    val appName1 = "app1"
    val ipAddress1 = InetAddress.getByName("127.0.0.1")
    val nodeApp1 = new Node("node1", ipAddress1, ChangeSet(0))

    def createApplications = {
      createNewApplication(deltasDir.toURI.toURL, appName1, TransformType.EXPANSION, Set[Node](nodeApp1))
    }

    def createConfigurations = {
      createNewConfiguration(deltasDir.toURI.toURL, List(appName1))
    }

    def after = {
      delete(deltasDir)
    }

    def delete(directory: File): Unit = {
      directory.listFiles() foreach { file =>
        if(file.isFile) {
          println(s"deleting file: ${file.getAbsolutePath}")
          file.delete()
        }
        else if(file.isDirectory){
          delete(file)
        }
      }
      println(s"deleting directory: ${directory.getAbsolutePath}")
      directory.delete()
    }

    def createNewApplication(deltasDirURL: URL, appName: String, transformType: TransformType, nodes: Set[Node]) = {
      val appConfigDir = new File(s"${deltasDirURL.toURI.getPath}/$appName")
      appConfigDir.mkdirs()
      val appConfigFile = new File(s"${appConfigDir.getAbsolutePath}/$appName.midas")
      appConfigFile.createNewFile()
      val appConfigText = s"""
                            |$appName {
                            |  mode = ${transformType.name.toLowerCase}
                            |  ${nodes mkString (NEW_LINE).trim}
                            |}
                           """.stripMargin
      println(s"writing to file: $appConfigText")
      write(appConfigText, appConfigFile)
    }


    val NEW_LINE = System.getProperty("line.separator")


    def createNewConfiguration(deltasDirURL: URL, appNames: List[String]) = {
      val configDir = new File(s"${deltasDirURL.toURI.getPath}")
      configDir.mkdirs()
      val configFile = new File(s"${configDir.getAbsolutePath}/midas.config")
      configFile.createNewFile()

      val configFileText = s"""
                              |apps {
                              |  ${appNames.mkString(NEW_LINE)}
                              |}
                             """.stripMargin
      write(configFileText, configFile)
    }

    def write(text: String, toFile: File) = {
      val writer = new PrintWriter(toFile, "utf-8")
      writer.write(text)
      writer.flush()
      writer.close()
    }
  }

  def is = sequential ^ s2"""
    Narration:
    //TODO: write a story to represent CRUD.
    This is a specification to verify that midas behaves as a proxy

    A client application should
        Step 1: Ensure Midas and mongods are running
            Create Application setup         $createApplication
            Start Midas                      $startMidas
            Connect to Midas                 $connect

        Step 2: Perform CRUD operations
            insert documents                 $insert
            read documents                   $read
            update documents                 $update
            delete documents                 $delete
            drop database                    $drop

        Step 3: Close connection to Midas
            Disconnect                       $disconnect
            Stop Midas                       $stopMidas
            Clean up the deltas              $deleteApplication
                                                               """

  def stopMidas = {
    MidasUtils.stopMidas(27020)
    true
  }
  def startMidas = {
    MidasUtils.startMidas(s"--port 27020 --deltasDir ${DeltasSetup.deltasDirPath}")
    true
  }

  def createApplication = {
    DeltasSetup.createApplications
    DeltasSetup.createConfigurations
    true
  }

  def deleteApplication = {
    DeltasSetup.delete(DeltasSetup.deltasDir)
    true
  }

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
