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
                 cluster which are at the new version and are connected to Midas. Schema migration is
                 in process, but they want to take down one node for maintenance for sometime. So,
                 Bob, the business analyst approaches Dave, the developer.

      Bob:  "Hey Dave, I know schema migration is in process but we need to take down NodeX for maintenance
             for sometime."
      Dave: "Ok Bob, thats not a problem. We just need to remove that node from application's config file.
             After that no requests from that node will be accepted. Once the node is up, we can again add
             it to the config and the requests from the node will be entertained like they were before."
      Bob:  "That will be great."

      1. To start out we have following documents in the database and this is simulated by inserting
         them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand(s"""db.orders.insert({name: "Vivek", "YourCart": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411006} })""")
              .runCommand(s"""db.orders.insert({name: "Komal", "YourCart": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411004} })""")
              .runCommand(s"""db.orders.insert({name: "Dhaval", "YourCart": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 110007} })""")
              .retrieve()
            form
         }

      2. There is a midas.config file in "deltas" folder
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

      3. There is a incyWincyShoppingApp.midas file in "incyWincyShoppingApp" folder in "deltas" folder
         with mode as expansion.
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

      4. There is a folder for change set "001AddToOrders" in "incyWincyShoppingApp" folder.
         ${
            changeSetDirPath = appDir + File.separator + "001AddToOrders"
            changeSetDir = Delta(changeSetDirPath, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("001AddToOrders")
            form
         }

      5. There is a delta file "0001_add_CountryToShippingAddress_transactions_orders.delta" to add
         "country" field to "ShippingAddress" at location "001AddToOrders" in "expansions" folder.
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

      7. Connect NodeX with midas and verify that it receives expanded documents
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocuments("orders")
              .verifyIfExpanded(noOfExpansions = 1)
              .retrieve()
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

      9. Try to connect from NodeX. It is not allowed to connect to midas.
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .runCommand("show collections")
              .retrieve()
            form
         }

      10. Lets say NodeX is up after maintenance now. So, we add NodeX to "incyWincyShoppingApp.midas" file
          in "incyWincyShoppingApp" folder in "deltas" folder
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

      11. Again connect from NodeX. It should see the expanded documents.
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocuments("orders")
              .verifyIfExpanded(noOfExpansions = 1)
              .retrieve()
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
              .retrieve()
            form
         }

      14. Cleanup deltas directory
         ${
            expansionDelta.delete("Delete Delta File", "0001_add_CountryToShippingAddress_transactions_orders.delta")
            changeSetDir.delete("Delete ChangeSet Folder", "")
            appConfigFile.delete("Delete Application File", "incyWincyShoppingApp.midas")
            configFile.delete("Delete Config File", "midas.config")
         }
                                                                                                            """

}
