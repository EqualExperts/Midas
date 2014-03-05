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
      Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. The new version of application
                 is connected to Midas. Schema migration is in process and Bob, the Business analyst wants
                 further more changes in the schema. So, he approaches Dave, the developer.
      Bob:  "Hi Dave. Can we incorporate more changes in the schema right now. We would like to display
             discount amount as well. We already have discount in percentage and Total amount. Will that
             be feasible."
      Dave: "Ok Bob. I would suggest that we can add one more changeSet for this in midas. So, Discount Amount
             will also be available after that."
      Bob:  "Thanks Dave."

      1. To start out we have following documents in the database and this is simulated by inserting
         them as shown below .
         ${
            val form = MongoShell("Open Mongo Shell", "localhost", 27017)
              .useDatabase("transactions")
              .runCommand(s"""db.orders.insert({name:"Vivek Dhapola", "YourCart": ['shoes', 'sipper'], "TotalAmount": 6000, "discount": 20, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411006} })""")
              .runCommand(s"""db.orders.insert({name:"Komal Jain", "YourCart": ['scarf', 'footwear'], "TotalAmount": 3000, "discount": 30, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 411004} })""")
              .runCommand(s"""db.orders.insert({name:"Dhaval Dalal", "YourCart": ['headsets'], "TotalAmount": 8000, "discount": 15, ShippingAddress: {line1: "enter house/street", line2: "enter city", "pincode": 110007} })""")
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

      4. There is a folder for change set "001SplitName" in "incyWincyShoppingApp" folder.
         ${
            changeSetDirPath1 = appDir + File.separator + "001SplitName"
            changeSetDir1 = Delta(changeSetDirPath1, () => "")
            var form = Form("Create ChangeSet Folder")
            val value = new File(changeSetDirPath1)
            form = form.tr("001SplitName")
            form
         }

      5. There is a delta file "0001_splitName_transactions_orders.delta" to split "name" into
         "firstName" and "lastName" at location "001SplitName" in "expansions" folder.
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

      7. Connect with midas and verify that it receives expanded documents with "name" field splited as
         "firstName" and "lastName".
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocuments("orders")
              .verifyIfAdded(Array("firstName", "lastName"), noOfExpansions = 1)
              .retrieve()
            form
         }

      8. Lets add one more change set "002AddDiscountAmount" folder in "incyWincyShoppingApp" folder.
         ${
            changeSetDirPath2 = appDir + File.separator + "002AddDiscountAmount"
            changeSetDir2 = Delta(changeSetDirPath2, () => "")
            var form = Form("Create ChangeSet Folder")
            val value = new File(changeSetDirPath2)
            form = form.tr("002AddDiscountAmount")
            form
         }

      9. Create a delta file "0001_calculateDiscountAmount_transactions_orders.delta" to calculate
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

      10. Connect with midas and verify that it receives expanded documents with "name" field splited as
         "firstName" and "lastName" and new field "Discount Amount".
         ${
            val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
              .useDatabase("transactions")
              .readDocuments("orders")
              .verifyIfAdded(Array("firstName", "lastName", "Discount Amount"), noOfExpansions = 2)
              .retrieve()
            form
         }

      11. Shutdown Midas
         ${
            val form = midasTerminal.stopMidas(27020)
            form
         }

      12. Clean up the database
         ${
            val form = MongoShell("Open MongoShell", "localhost", 27017)
               .useDatabase("transactions")
               .runCommand("""db.dropDatabase()""")
               .retrieve()
            form
         }

      13. Cleanup deltas directory
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
