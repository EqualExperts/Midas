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
import org.specs2.specification
import specification._
import com.ee.midas.fixtures.{CommandTerminal, Delta, MongoShell}
import java.io.File
import org.specs2.form.Form

class AddAndRemoveApplicationJourney extends Specification with Forms {

    var baseDeltaDir: String = null
    var configFile: Delta = null
    var app1Dir: String = null
    var app1ConfigFile: Delta = null
    var app2Dir: String = null
    var app2ConfigFile: Delta = null
    var changeSetDirApp1: Delta = null
    var changeSetDirPathApp1: String = null
    var changeSetDirApp2: Delta = null
    var changeSetDirPathApp2: String = null
    var expansionDeltaApp1: Delta = null
    var expansionDeltaApp2: Delta = null
    var midasTerminal = CommandTerminal("")

    sequential
    def is = s2"""
      ${"Add/Remove Application On the fly".title}
      Narration: IncyWincyShoppingApp and IncyWincyTravelApp stores its persistent data on MongoDB.
                 IncyWincyShoppingApp is already supported by Midas.  Folks at the IncyWincyTravelApp
                 learnt from IncyWincyShoppingApp developers that schema migration need not be painful. It 
                 can be done systematically using Midas.  
                 So, they approach Oscar, the DevOps guy and request him to make IncyWincyTravelApp
                 Midas enabled.
                 Dave, a developer from their team works with Oscar to see how that can be possible.

      Dave:  "Hey Oscar. IncyWincyTravelApp also needs Midas support for dealing with schema changes.
              Do we need to create a new midas instance for that? "
      Oscar: "No Dave, not at all. Midas can support multiple applications at a time"
      Dave:  "What information or artifacts do you need from me to make this happen?"
      Oscar: "All, I need is the delta files grouped by expansions/contractions folder within a changeset
              folder and all of such changeset folders"
      Dave:  "ok, so we will zip and ship those!"
      Oscar: "Thats correct.  From there on, I'll create a folder for IncyWincyTravelApp within Midas' deltas
              directory, copy in the changesets that you gave me.  I'll also create IncyWincyTravelApp.midas
              and put your Application Node IPs along with ChangeSet and mode. After that I'll tell Midas to
              start seeing IncyWincyTravelApp by changing midas.config."
      Dave:  "Would that mean any down-time for IncyWincyShoppingApp?"
      Oscar: "No, We can add or remove applications from Midas at runtime, without causing outages."



      1. To start out we have following documents in the IncyWincyShoppingApp database and this is simulated by
         inserting them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand(s"""db.orders.insert({name: "Vivek", "MobileNo": "9123456789", "OrderList": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411006} })""")
              .runCommand(s"""db.orders.insert({name: "Komal", "MobileNo": "9223455677", "OrderList": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411004} })""")
              .runCommand(s"""db.orders.insert({name: "Dhaval", "MobileNo": "9333455698", "OrderList": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 110007} })""")
              .build
            form
         }

      2. IncyWincyShoppingApp is already added to midas.config file in "deltas" folder
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

      3. There is a "incyWincyShoppingApp" folder in "deltas" with incyWincyShoppingApp.midas file having
         its Node information and mode.
         ${
            app1Dir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
            app1ConfigFile = Delta(app1Dir, () => {
              """incyWincyShoppingApp {
                   mode = expansion
                   nodeY {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                 }
              """
            })
            val form = app1ConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
            form
         }

      4. "incyWincyShoppingApp" has one change set "001TransformNumber".
         ${
            changeSetDirPathApp1 = app1Dir + File.separator + "001TransformNumber"
            changeSetDirApp1 = Delta(changeSetDirPathApp1, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("001TransformNumber")
            form
         }

      5. "001TransformNumber" changeset has a delta file "0001_tranformMobileNo_transactions_orders.delta"
         to append "+91" to "Mobile No" in "expansions" folder.
         ${
            val expansionDeltaDirApp1 = changeSetDirPathApp1 + File.separator + "expansions"
            expansionDeltaApp1 = Delta(expansionDeltaDirApp1, () => {
              """use transactions
                       db.orders.transform('MobileNo', '{ $concat: ["+91", "$MobileNo"] }')
              """
            })
            val form = expansionDeltaApp1.saveAs("Write Delta", "0001_tranformMobileNo_transactions_orders.delta")
            form
         }

      6. Midas is running with deltas directory location as "deltas"
         ${
            midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + File.separator + baseDeltaDir)
            val form = midasTerminal.startMidas
            form
         }

      7. We have following documents in the IncyWincyTravelApp database and this is simulated by inserting
         them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("users")
              .runCommand(s"""db.customers.insert({"firstName": "Vivek", "lastName": "Dhapola", "age": 25, "emailId": "vdhapola@equalexperts.com" })""")
              .runCommand(s"""db.customers.insert({"firstName": "Komal", "lastName": "Jain", "age": 23, "emailId": "kjain@equalexperts.com" })""")
              .runCommand(s"""db.customers.insert({"firstName": "Dhaval", "lastName": "Dalal", "age": 38, "emailId": "ddalal@equalexperts.com" })""")
              .build
            form
          }

      8. We create incyWincyTravelApp.midas file with all the nodes ips and changeset information given in
         "incyWincyTravelApp" folder with mode as expansion.
         ${
            app2Dir = baseDeltaDir + File.separator + "incyWincyTravelApp"
            app2ConfigFile = Delta(app2Dir, () => {
              """incyWincyTravelApp {
                   mode = expansion
                   nodeA {
                     ip = 127.0.0.1
                     changeSet = 1
                   }
                 }
              """
            })
            val form = app2ConfigFile.saveAs("Write Application Config File", "incyWincyTravelApp.midas")
            form
         }

      9. Create a folder for change set "001MergeName" in "incyWincyTravelApp" folder.
         ${
            changeSetDirPathApp2 = app2Dir + File.separator + "001MergeName"
            changeSetDirApp2 = Delta(changeSetDirPathApp2, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("001MergeName")
            form
         }

      10. Create a delta file "0001_MergeIntoName_users_customers.delta" to merge "firstName" and
          "lastName" into "Name" at location "001MergeName" in "expansions" folder.
         ${
            val expansionDeltaDirApp2 = changeSetDirPathApp2 + File.separator + "expansions"
            expansionDeltaApp2 = Delta(expansionDeltaDirApp2, () => {
              """use users
                 db.customers.merge('["firstName", "lastName"]', ' ', 'Name')
              """
            })
            val form = expansionDeltaApp2.saveAs("Write Delta", "0001_MergeIntoName_users_customers.delta")
            form
         }

      11. Add application incyWincyTravelApp to midas.config file in "deltas" folder
         ${
            baseDeltaDir = "/deltas"
            configFile = Delta(baseDeltaDir, () => {
              """
                |apps {
                |  incyWincyShoppingApp
                |  incyWincyTravelApp
                |}
              """.stripMargin
            })
            val form = configFile.saveAs("Write Config File", "midas.config")
            form
          }

      12. IncyWincyTravelApp connects with midas and starts receiving expanded documents.
         ${
            val form = MongoShell("IncyWincyTravelApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("users")
              .readDocumentsFromCollection("customers")
              .assertFieldsAdded(Array("Name"), expansionVersion = 1)
              .build
            form
         }

      13. Shutdown Midas
         ${
            val form = midasTerminal.stopMidas(27020)
            form
         }

      14. Clean up the database
         ${
            val form = MongoShell("Open MongoShell", "localhost", 27017)
              .useDatabase("users")
              .runCommand("""db.dropDatabase()""")
              .useDatabase("transactions")
              .runCommand("""db.dropDatabase()""")
              .build
            form
         }

      15. Cleanup deltas directory
         ${
            expansionDeltaApp2.delete("Delete Delta File", "0001_MergeIntoName_users_customers.delta")
            changeSetDirApp2.delete("Delete ChangeSet Folder", "")
            expansionDeltaApp1.delete("Delete Delta File", "0001_tranformMobileNo_transactions_orders.delta")
            changeSetDirApp1.delete("Delete ChangeSet Folder", "")
            app2ConfigFile.delete("Delete Application File", "incyWincyTravelApp.midas")
            app1ConfigFile.delete("Delete Application File", "incyWincyShoppingApp.midas")
            configFile.delete("Delete Config File", "midas.config")
         }
                                                                                                      """
}
