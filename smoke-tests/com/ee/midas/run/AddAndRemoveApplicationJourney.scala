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
      Narration: IncyWincyShoppingApp and IncyWincyTravelBookingApp stores its persistent data on MongoDB.
                 IncyWincyShoppingApp is connected to Midas and its schema migration is in process. IncyWincy
                 organisation wants to some schema changes for IncyWincyTravelBookingApp as well. Bob,
                 the business analyst approaches Dave, the developer.

      Bob:  "Hey Dave. IncyWincyTravelBookingApp also need certain changes in schema. We need fullname for the
             users. Do we need to create a new midas instance for that? "
      Dave: "No Bob, not at all. Midas can support multiple applications at a time. We just need to add that
             application to midas.config and that application will be picked up by midas automatically and
             will start receiving transformed documents according to the deltas provided."
      Bob:  "Cool .So, Can you do the needful for that Dave?"
      Dave: "Ya sure."
      Bob:  "Thanks."

      1. To start out we have following documents in the IncyWincyTravelBookingApp database and this is
         simulated by inserting them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("users")
              .runCommand(s"""db.customers.insert({"firstName": "Vivek", "lastName": "Dhapola", "age": 25, "emailId": "vdhapola@equalexperts.com" })""")
              .runCommand(s"""db.customers.insert({"firstName": "Komal", "lastName": "Jain", "age": 23, "emailId": "kjain@equalexperts.com" })""")
              .runCommand(s"""db.customers.insert({"firstName": "Dhaval", "lastName": "Dalal", "age": 38, "emailId": "ddalal@equalexperts.com" })""")
              .retrieve()
            form
         }

      2. We have following documents in the IncyWincyShoppingApp database and this is simulated by
         inserting them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand(s"""db.orders.insert({name: "Vivek", "MobileNo": "9123456789", "OrderList": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411006} })""")
              .runCommand(s"""db.orders.insert({name: "Komal", "MobileNo": "9223455677", "OrderList": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411004} })""")
              .runCommand(s"""db.orders.insert({name: "Dhaval", "MobileNo": "9333455698", "OrderList": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 110007} })""")
              .retrieve()
            form
         }

      3. There is a midas.config file in "deltas" folder
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

      4. There is a incyWincyShoppingApp.midas file in "incyWincyShoppingApp" folder in "deltas" folder
         with mode as expansion.
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

      5. There is a folder for change set "001TransformNumber" in "incyWincyShoppingApp" folder.
         ${
            changeSetDirPathApp1 = app1Dir + File.separator + "001TransformNumber"
            changeSetDirApp1 = Delta(changeSetDirPathApp1, () => "")
            var form = Form("Create ChangeSet Folder")
            form = form.tr("001TransformNumber")
            form
         }

      6. There is a delta file "0001_tranformMobileNo_transactions_orders.delta" to append "+91" to
         "Mobile No" at location "001TransformNumber" in "expansions" folder.
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

      7. Midas is running with deltas directory location as "deltas"
         ${
            midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + File.separator + baseDeltaDir)
            val form = midasTerminal.startMidas
            form
         }

      8. We want to add IncyWincyTravelBookingApp to midas. So, we create incyWincyTravelBookingApp.midas
         file in "incyWincyTravelBookingApp" folder in "deltas" folder with mode as expansion.
         ${
            app2Dir = baseDeltaDir + File.separator + "incyWincyTravelBookingApp"
            app2ConfigFile = Delta(app2Dir, () => {
              """incyWincyTravelBookingApp {
                   mode = expansion
                   nodeA {
                     ip = 127.0.0.1
                     changeSet = 1
                   }
                 }
              """
            })
            val form = app2ConfigFile.saveAs("Write Application Config File", "incyWincyTravelBookingApp.midas")
            form
         }

      9. Create a folder for change set "001MergeName" in "incyWincyTravelBookingApp" folder.
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

      11. Add application incyWincyTravelBookingApp to midas.config file in "deltas" folder
         ${
            baseDeltaDir = "/deltas"
            configFile = Delta(baseDeltaDir, () => {
              """
                |apps {
                |  incyWincyShoppingApp
                |  incyWincyTravelBookingApp
                |}
              """.stripMargin
            })
            val form = configFile.saveAs("Write Config File", "midas.config")
            form
          }

      12. Connect IncyWincyTravelBookingApp with midas and verify that it receives expanded documents with
          "Name" field.
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("users")
              .readDocuments("customers")
              .verifyIfAdded(Array("Name"), noOfExpansions = 1)
              .retrieve()
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
              .retrieve()
            form
         }

      15. Cleanup deltas directory
         ${
            expansionDeltaApp2.delete("Delete Delta File", "0001_MergeIntoName_users_customers.delta")
            changeSetDirApp2.delete("Delete ChangeSet Folder", "")
            expansionDeltaApp1.delete("Delete Delta File", "0001_tranformMobileNo_transactions_orders.delta")
            changeSetDirApp1.delete("Delete ChangeSet Folder", "")
            app2ConfigFile.delete("Delete Application File", "incyWincyTravelBookingApp.midas")
            app1ConfigFile.delete("Delete Application File", "incyWincyShoppingApp.midas")
            configFile.delete("Delete Config File", "midas.config")
         }
                                                                                                      """
}
