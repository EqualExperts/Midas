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

package com.ee.midas.model

import java.net.{Socket, InetAddress, URL}
import java.io.File
import org.specs2.mutable.After
import com.ee.midas.transform.TransformType


trait EntireConfigurationSetup extends After {
  val deltasDir: File

  val appName1 = "app1"
  val ipAddress1 = InetAddress.getByName("127.0.0.1")
  val nodeApp1 = new Node("node1", ipAddress1, ChangeSet(1))

  val appName2 = "app2"
  val ipAddress2 = InetAddress.getByName("127.0.0.2")
  val nodeApp2 = new Node("node2", ipAddress2, ChangeSet(1))

  val appName3 = "app3"
  val ipAddress3 = InetAddress.getByName("127.0.0.3")
  val nodeApp3 = new Node("node3", ipAddress3, ChangeSet(2))
  val servers = new ServerSetup()

  def createApplications = {
    val application1 = createNewApplication(deltasDir.toURI.toURL, appName1, TransformType.EXPANSION, Set[Node](nodeApp1))
    val application2 = createNewApplication(deltasDir.toURI.toURL, appName2, TransformType.EXPANSION, Set[Node](nodeApp2))
    val application3 = createNewApplication(deltasDir.toURI.toURL, appName3, TransformType.EXPANSION, Set[Node](nodeApp3))
    (application1, application2, application3)
  }

  def createSockets = {
    servers.start
    val midasClient = new Socket("localhost", servers.midasServerPort)
    val mongoClient = new Socket("localhost", servers.mongoServerPort)
    (midasClient, mongoClient)
  }

  def stopSockets = {
    servers.stop
  }

  val oldConfiguration: Configuration = null
  val newConfiguration: Configuration = null

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
    new Application(appConfigDir.toURI.toURL, appName, transformType, nodes)
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
    new Configuration(deltasDirURL, appNames)
  }
}