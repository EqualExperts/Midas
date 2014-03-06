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

class AddAndRemoveChangeSetJourney extends Specification with Forms {

    var baseDeltaDir: String = null
    var configFile: Delta = null
    var appDir: String = null
    var appConfigFile: Delta = null
    var changeSetDir1: Delta = null
    var changeSetDirPath1: String = null
    var changeSetDir2: Delta = null
    var changeSetDirPath2: String = null
    var expansionDelta1: Delta = null
    var expansionDelta2: Delta = null
    var midasTerminal = CommandTerminal("")

    sequential
    def is = s2"""
      ${"Add/Remove ChangeSet On the fly".title}
      Narration: Bob, the Business analyst wanted to develop a loyalty programme for IncyWincyShoppingApp.
                 Developers have baked that feature into the Application and the new version of application is
                 now ready for release.  So Dave, the developer approaches Oscar, the DevOps guy.

      Dave:  "Oscar, I have the new ChangeSets zipped up containing the schema changes for Loyalty Programme.
              Can you help us with the release of the new version of the application?"

      Oscar: "Sure Dave, I'll take those offline nodes and upgrade the application to this new version first."

      Oscar: "Further, after that I'll update the IncyWincyShoppingApp.midas offline nodes changesets to the
              highest changeset number.  Following this, I'll throw in the change set folders that you gave
              me, in to the IncyWincyShoppingApp folder.  Once all that is done, I'll flip the Load balancer
              to route the traffic through these nodes."
      Dave:  "Ok Oscar. That sounds like an approach.  What if, we have issues with the new version and it
              becomes unstable?"
      Oscar: "There are 2 approaches.  1. We could temporarily rollback to the old version of the application,
              by simply flipping the Load Balancer back and in the mean time you guys work on the fixes and we
              can then redeploy. After re-deployment, I'll flip the Load Balancer back again to those nodes
              where we have the new version of the App.
              2. We could keep the newer version application running, if the problem does not cripple the
              application completely.  While you can work towards the fix and we will re-deploy the newer
              fixed version."
      Dave:  "Ok that sounds reasonable."
      Dave:  "Oscar, what if we had to change some schema for that fix?"
      Oscar: "Well Dave, remember that with Midas, you reverse a change by a counter-change and you always move
              forward in time.  So as long as you respect that and not modify old schema transformations in delta
              files, we will all be good."

      1. To start out we have following documents in the database and this is simulated by inserting
         them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand(s"""db.orders.insert({name: "Vivek Dhapola", "YourCart": ['shoes', 'sipper'], "TotalAmount": 6000, "discount": 20, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411006} })""")
              .runCommand(s"""db.orders.insert({name: "Komal Jain", "YourCart": ['scarf', 'footwear'], "TotalAmount": 3000, "discount": 30, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411004} })""")
              .runCommand(s"""db.orders.insert({name: "Dhaval Dalal", "YourCart": ['headsets'], "TotalAmount": 8000, "discount": 15, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 110007} })""")
              .build
            form
         }

      2. "incyWincyShoppingApp" is already added to midas.config file in "deltas" folder
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

      3. incyWincyShoppingApp.midas file in "incyWincyShoppingApp" folder contain all nodes information
         with mode as expansion. NodeX is offline and traffic is routed through NodeY currently.
         ${
            appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
            appConfigFile = Delta(appDir, () => {
              """incyWincyShoppingApp {
                   mode = expansion
                   NodeX {
                     ip = 127.0.0.1
                     changeSet = 1
                   }
                   NodeY {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                 }
              """
            })
            val form = appConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
            form
          }

      4. There is a already a change set "001SplitName" in "incyWincyShoppingApp" folder.
         ${
            changeSetDirPath1 = appDir + File.separator + "001SplitName"
            changeSetDir1 = Delta(changeSetDirPath1, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("001SplitName")
            form
         }

      5. changeset "001SplitName" has a delta file "0001_splitName_transactions_orders.delta" to split
         "name" into firstName" and "lastName" in "expansions" folder.
         ${
            val expansionDeltaDir = changeSetDirPath1 + File.separator + "expansions"
            expansionDelta1 = Delta(expansionDeltaDir, () => {
              """use transactions
                 db.orders.split('name', '^([a-zA-Z]+) ([a-zA-Z]+)$', '{"firstName": "$1", "lastName": "$2" }')
              """
            })
            val form = expansionDelta1.saveAs("Write Delta", "0001_splitName_transactions_orders.delta")
            form
         }

      6. Midas is running with deltas directory location as "deltas"
         ${
            midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + File.separator + baseDeltaDir)
            val form = midasTerminal.startMidas
            form
         }

      7. NodeY connects with midas and it receives expanded documents with "name" field splited as
         "firstName" and "lastName".
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocumentsFromCollection("orders")
              .assertFieldsAdded(Array("firstName", "lastName"), expansionVersion = 1)
              .build
            form
         }

      8. NodeX is upgraded to new version of application. Updating NodeX to highest changeSet in
         "incyWincyShoppingApp.midas".
         ${
            appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
            appConfigFile = Delta(appDir, () => {
              """incyWincyShoppingApp {
                   mode = expansion
                   NodeX {
                     ip = 127.0.0.1
                     changeSet = 2
                   }
                   NodeY {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                 }
              """
            })
            val form = appConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
            form
         }

      9. Adding change set "002AddDiscountAmount" folder in "incyWincyShoppingApp" folder.
         ${
            changeSetDirPath2 = appDir + File.separator + "002AddDiscountAmount"
            changeSetDir2 = Delta(changeSetDirPath2, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("002AddDiscountAmount")
            form
         }

      10. Create a delta file "0001_calculateDiscountAmount_transactions_orders.delta" to calculate
         "Discount Amount" at location "002AddDiscountAmount" in "expansions" folder.
         ${
            val expansionDeltaDir = changeSetDirPath2 + File.separator + "expansions"
            expansionDelta2 = Delta(expansionDeltaDir, () => {
              """use transactions
                 db.orders.transform('Discount Amount', '{ $multiply: ["$TotalAmount", { $divide: ["$discount", 100] }] }')
              """
            })
            val form = expansionDelta2.saveAs("Write Delta", "0001_calculateDiscountAmount_transactions_orders.delta")
            form
         }

      11. Now NodeX is online and traffic is routed through NodeX. It connects with midas and it starts
          receiving expanded documents.
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocumentsFromCollection("orders")
              .assertFieldsAdded(Array("firstName", "lastName", "Discount Amount"), expansionVersion = 2)
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
            expansionDelta2.delete("Delete Delta File", "0001_calculateDiscountAmount_transactions_orders.delta")
            expansionDelta1.delete("Delete Delta File", "0001_splitName_transactions_orders.delta")
            changeSetDir1.delete("Delete ChangeSet Folder", "")
            changeSetDir2.delete("Delete ChangeSet Folder", "")
            appConfigFile.delete("Delete Application File", "incyWincyShoppingApp.midas")
            configFile.delete("Delete Config File", "midas.config")
         }
                                                                                                """
}
