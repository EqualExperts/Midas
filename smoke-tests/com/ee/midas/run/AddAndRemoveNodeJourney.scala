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

package com.ee.midas.run

import org.specs2._
import specification._
import com.ee.midas.fixtures.{CommandTerminal, Delta, MongoShell}
import java.io.File
import org.specs2.form.Form

class AddAndRemoveNodeJourney extends Specification with Forms {

    var baseDeltaDir: String = null
    var configFile: Delta = null
    var appDir: String = null
    var appConfigFile: Delta = null
    var changeSetDir: Delta = null
    var changeSetDirPath: String = null
    var expansionDelta: Delta = null
    var midasTerminal = CommandTerminal("")

    sequential
    def is = s2"""
      ${"Add/Remove Node On the fly".title}
      Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. It has 2 nodes in the
                 cluster which are at the new version and are connected to Midas.
                 Adam, the admin needs to take down one node for scheduled maintenance. So, he
                 approaches Oscar, the DevOps guy.

      Adam:  "Hey Oscar, We need to take down NodeX for a scheduled maintenance and give it back to you in
              about couple of hours.  How do we go about that?"
      Oscar: "Ok Adam, thats not a problem. We just need to remove that node from application's config file
              and the Load Balancer. After that no requests from that node will be accepted.
              Once the node is up, we can again add it to the application config file and the Load balancer
              and immediately after that the requests from the node will be entertained like they
              were before."
      Adam:  "By any chance would Midas be shutdown during the whole process? In other words any
              disservice to already connected nodes and clients?"
      Oscar: "No, all this happens at runtime and Midas will be running all the time.  There will
              be no issues to clients connected to other Nodes of the same application."
      Adam:  "Ok, That will be great."

      1. To start out we have following documents in the database and this is simulated by inserting
         them as shown below.
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand(s"""db.orders.insert({name: "Vivek", "YourCart": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411006} })""")
              .runCommand(s"""db.orders.insert({name: "Komal", "YourCart": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411004} })""")
              .runCommand(s"""db.orders.insert({name: "Dhaval", "YourCart": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 110007} })""")
              .build
            form
         }

      2. IncyWincyShoppingApp is already added to midas.config file in "deltas" folder.
         ${
            baseDeltaDir = "/deltas"
            configFile = Delta(baseDeltaDir, () => {
              """
                |apps {
                |  incyWincyShoppingApp
                |}
              """.stripMargin
            })
            val form = configFile.saveAs("Write Config File", "midas.config")
            form
         }

      3. There is a "incyWincyShoppingApp" folder in "deltas" with "incyWincyShoppingApp.midas" file having
         its Node information and mode.
         ${
            appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
            appConfigFile = Delta(appDir, () => {
              """incyWincyShoppingApp {
                   mode = expansion
                   nodeX {
                     ip = 127.0.0.1
                     changeSet = 1
                   }
                   nodeY {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                 }
              """
            })
            val form = appConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
            form
         }

      4. "incyWincyShoppingApp" has one change set "001AddToOrders".
         ${
            changeSetDirPath = appDir + File.separator + "001AddToOrders"
            changeSetDir = Delta(changeSetDirPath, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("001AddToOrders")
            form
         }

      5. "001AddToOrders" changeset has a delta file
         "0001_add_CountryToShippingAddress_transactions_orders.delta" to add "country"
         field to "ShippingAddress" in "expansions" folder.
         ${
            val expansionDeltaDir = changeSetDirPath + File.separator + "expansions"
            expansionDelta = Delta(expansionDeltaDir, () => {
              """use transactions
                 db.orders.add('{"ShippingAddress.country": "India"}')
              """
            })
            val form = expansionDelta.saveAs("Write Delta", "0001_add_CountryToShippingAddress_transactions_orders.delta")
            form
         }

      6. Midas is running with deltas directory location as "deltas"
         ${
            midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + File.separator + baseDeltaDir)
            val form = midasTerminal.startMidas
            form
         }

      7. NodeX connects with midas and it receives expanded documents
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocumentsFromCollection("orders")
              .assertAllDocumentsHaveExpanded(expansionVersion = 1)
              .build
            form
         }

      8. Now NodeX is down for maintenance. So, we remove NodeX from "incyWincyShoppingApp.midas" file
         in "incyWincyShoppingApp" folder in "deltas" folder
         ${
            appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
            appConfigFile = Delta(appDir, () => {
              """incyWincyShoppingApp {
                   mode = expansion
                   nodeY {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                 }
              """
            })
            val form = appConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
            form
         }

      9. NodeX try to connect. It is not allowed to connect to midas.
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .runCommand("show collections")
              .build
            form
         }

      10. NodeX is up after maintenance now. So, we add NodeX to "incyWincyShoppingApp.midas" file in
          "incyWincyShoppingApp" folder in "deltas" folder
         ${
            appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
            appConfigFile = Delta(appDir, () => {
              """incyWincyShoppingApp {
                   mode = expansion
                   nodeX {
                     ip = 127.0.0.1
                     changeSet = 1
                   }
                   nodeY {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                 }
              """
            })
            val form = appConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
            form
         }

      11.  NodeX connects to midas. It starts receiving the expanded documents.
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocumentsFromCollection("orders")
              .assertAllDocumentsHaveExpanded(expansionVersion = 1)
              .build
            form
         }

      12. Shutdown Midas
         ${
            val form = midasTerminal.stopMidas(27020)
            form
         }

      13. Clean up the database
         ${
            val form = MongoShell("Open MongoShell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand("""db.dropDatabase()""")
              .build
            form
         }

      14. Cleanup deltas directory
         ${
            expansionDelta.delete("Delete Delta File", "0001_add_CountryToShippingAddress_transactions_orders.delta")
            changeSetDir.delete("Delete ChangeSet Folder", "")
            appConfigFile.delete("Delete Application File", "incyWincyShoppingApp.midas")
            configFile.delete("Delete Deltas Directory", "midas.config")
         }
                                                                                                            """

}
