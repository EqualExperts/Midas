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
import com.ee.midas.fixtures.{Delta, CommandTerminal, MongoShell}
import java.io.File
import org.specs2.form.Form
import com.mongodb.{DBObject, BasicDBObject}

class RenameJourney extends Specification with Forms {

  var midasTerminal = CommandTerminal("")
  var expansionDelta1: Delta = null
  var expansionDelta2: Delta = null
  var contractionDelta: Delta = null
  var configFile: Delta = null
  var appConfigFile: Delta = null
  var changeSetDir: Delta = null
  var baseDeltaDir: String = null
  var appDir: String = null
  var changeSetDirPath: String = null

  sequential
  def is = s2"""
    ${"Rename Journey".title}
    Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. Dave, the Developer
               wanted to rename certain things so that it closely modeled the domain.  So,
               he approaches Oscar, the DevOps guy.

    Dave:  "Hey Oscar, Lets rename zip to pin in address because it is closer to the domain.
           Also, Let's rename OrderList as Carts as they are more closer to the domain"
    Oscar: "Ya Dave, that makes sense!"
    Dave:  "How do you plan to do that Oscar ? Do we need to have some downtime?"
    Oscar: "No Dave, We will use Midas which will migrate our schema on the fly. Here is what we can do
           for zero downtime deployment. First, we will run Expansion scripts and copy the current
           field to new field. This will keep our application backwards compatible with the existing
           version."
    Dave:  "Oh.. So Expansion will add the new field and keep the old field as well."
    Oscar: "Yes exactly."
    Dave:  "Okay, but we have 2 nodes in our cluster. So, Do we apply this to all nodes
           simultaneously."
    Oscar: "Yes, All nodes of a cluster will be at the same version at a time. Once the system is
           completely upgraded and deemed stable, we will run the contraction scripts and remove the
           old field."
    Dave:  "Okay, but what if after adding new field system is not stable . Do we need to rollback DB?"
    Oscar: "No Dave. DB Rollback can lead to loss in data and leave database in inconsistent state.
           In that case it will be better to rollback application instead."
    Dave:  "Oh ... right . That makes sense."
    Oscar: "So after the Expansion and Contraction cycle, the system will be migrated completely."
    Dave:  "Ok. I understand, that sounds good."
    Dave:  "Also, one more thought that just crossed my mind. Do we need to incorporate some
           changes in the application to do migration with Midas, for example, to use optimistic
           locking with Hibernate, we need to add a version field in the Domain Model."
    Oscar: "No Dave, we don't need to incorporate any change in the domain model or application.
           Midas takes care of that. It injects _expansionVersion and _contractionVersion field in
           the document when the application inserts or updates the document."
    Dave:  "Ok, great."


    1. To start out we have following documents in the database and this is simulated by inserting
       them as shown below .
       ${
          val form = MongoShell("Open Mongo Shell", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand(s"""db.orders.insert({name: "Vivek", "OrderList": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411006} })""")
            .runCommand(s"""db.orders.insert({name: "Komal", "OrderList": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411004} })""")
            .runCommand(s"""db.orders.insert({name: "Dhaval", "OrderList": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 110007} })""")
            .build()
          form
       }

    2. Create a midas.config file in "deltas" folder
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

    3. Create a incyWincyShoppingApp.midas file in "incyWincyShoppingApp" folder in "deltas" folder
       with mode as expansion.
       ${
          appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
          appConfigFile = Delta(appDir, () => {
            """incyWincyShoppingApp {
                   mode = expansion
                   nodeA {
                     ip = 127.0.0.1
                     changeSet = 1
                   }
                   nodeB {
                     ip = 192.168.1.41
                     changeSet = 1
                   }
                }
            """
          })
          val form = appConfigFile.saveAs("Write Application Config File", "incyWincyShoppingApp.midas")
          form
       }

    4. Create a folder for change set "001RenameOrders" in "incyWincyShoppingApp" folder.
       ${
          changeSetDirPath = appDir + File.separator + "001RenameOrders"
          changeSetDir = Delta(changeSetDirPath, () => "")
          var form = Form("Create ChangeSet Folder")
          form = form.tr("001RenameOrders")
          form
       }

    5. Create delta file "0001_copy_transactions_orders_OrderListToCartsField.delta" to copy "OrderList"
       into "Carts" at location "001RenameOrders" in "expansions" folder
       ${
          val expansionDeltaDir = changeSetDirPath + File.separator + "expansions"
          expansionDelta1 = Delta(expansionDeltaDir, () => {
            """use transactions
               db.orders.copy('OrderList','Carts')
            """
          })
          val form = expansionDelta1.saveAs("Write Delta", "0001_copy_transactions_orders_OrderListToCartsField.delta")
          form
       }

    6. Create delta file "0002_copy_transactions_orders_ZipcodeToPincodeField.delta" to copy "zipcode"
       to "pincode" at location "001RenameOrders" in "expansions" folder
       ${
          val expansionDeltaDir = changeSetDirPath + File.separator + "expansions"
          expansionDelta2 = Delta(expansionDeltaDir, () => {
            """use transactions
               db.orders.copy("ShippingAddress.zipcode", "ShippingAddress.pincode")
            """
          })
          val form = expansionDelta2.saveAs("Write Delta", "0002_copy_transactions_orders_ZipcodeToPincodeField.delta")
          form
       }

    7. Create delta file "0001_removeFrom_transactions_orders_OrderListField.delta" to remove "OrderList"
        and "ShippingAddress.zipcode" at location "001RenameOrders" in "contraction" folder
        ${
           val contractionDeltaDir = changeSetDirPath + File.separator + "contractions"
           contractionDelta = Delta(contractionDeltaDir, () => {
              """use transactions
                 db.orders.remove("['OrderList']")
                 db.orders.remove("['ShippingAddress.zipcode']")
              """
           })
           val form = contractionDelta.saveAs("Write Delta", "0001_removeFrom_transactions_orders_OrderListField.delta")
           form
        }

    8. Start Midas with deltas directory location as "deltas"
       ${
          midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + File.separator + baseDeltaDir)
          val form = midasTerminal.startMidas
          form
       }

    9. Connect with midas and verify that read documents contain new fields "Carts" and
       "ShippingAddress.pincode"
       ${
          val orderListToCarts = "OrderList" -> "Carts"
          val zipCodeToPinCode = "ShippingAddress.zipcode" -> "ShippingAddress.pincode"
          val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
            .useDatabase("transactions")
            .readDocumentsFromCollection("orders")
            .assertAllDocumentsHaveEqualValuesInNewAndOldFields(Array(orderListToCarts, zipCodeToPinCode), expansionVersion = 2)
            .build()
          form
       }

    10. WebApp updates and write back the documents to database. Midas inserts expansionVersion in
        the document on the way back.
       ${
          val updateDocument1 = new BasicDBObject("Carts", Array("shoes", "sipper"))
                                .append("ShippingAddress.pincode", 411006)
          val updateDocument2 = new BasicDBObject("Carts", Array("scarf", "footwear"))
                                .append("ShippingAddress.pincode", 411004)
          val updateDocument3 = new BasicDBObject("Carts", Array("headsets"))
                                .append("ShippingAddress.pincode", 110007)
          val form =  MongoShell("IncyWincyShoppingApp - UpgradedVersion", "127.0.0.1", 27020)
            .useDatabase("transactions")
            .update("orders", new BasicDBObject("name", "Vivek"), new BasicDBObject("$set", updateDocument1))
            .update("orders", new BasicDBObject("name", "Komal"), new BasicDBObject("$set", updateDocument2))
            .update("orders", new BasicDBObject("name", "Dhaval"), new BasicDBObject("$set", updateDocument3))
            .build()
          form
       }

    11. Assure that all the documents are expanded. We will simulate this here by connecting to mongo
        directly as shown below:
       ${
          val form = MongoShell("IncyWincyShoppingApp - Expansion Complete", "127.0.0.1", 27017)
            .useDatabase("transactions")
            .readDocumentsFromCollection("orders")
            .assertAllDocumentsHaveExpanded(expansionVersion = 2)
            .build
          form
       }

    12. Change mode of Application to "CONTRACTION" in "incyWincyShoppingApp.midas" file.
       ${
          appDir = baseDeltaDir + File.separator + "incyWincyShoppingApp"
          appConfigFile = Delta(appDir, () => {
            """incyWincyShoppingApp {
                 mode = contraction
                 nodeA {
                   ip = 127.0.0.1
                   changeSet = 1
                 }
                 nodeB {
                   ip = 192.168.1.41
                   changeSet = 1
                 }
              }
            """
          })
          val form = appConfigFile.saveAs("Edit Application Config File", "incyWincyShoppingApp.midas")
          form
       }

    13. Read documents and verify that "OrderList" and "ShippingAddress.zipcode" fields are removed from documents.
       ${
          val form = MongoShell("Open Command Terminal", "127.0.0.1", 27020)
            .useDatabase("transactions")
            .readDocumentsFromCollection("orders")
            .assertFieldsRemoved(Array("OrderList", "ShippingAddress.zipcode"), contractionVersion = 2)
            .build()
          form
       }

    14. Insert a document from the Upgraded Version of the app.
       ${
          val address: DBObject = new BasicDBObject("line1", "enter house/street")
                                  .append("line2", "enter city")
                                  .append("pincode", 411006)
          val insertDocument: DBObject = new BasicDBObject("name", "Pooja")
                                         .append("Carts", Array("dress"))
                                         .append("TotalAmount", 1000)
                                         .append("ShippingAddress", address)
          val form = MongoShell("Open Mongo Shell", "localhost", 27020)
            .useDatabase("transactions")
            .insert("orders", insertDocument)
            .build()
          form
       }

    15. Read documents and verify that all documents are contracted.
       ${
           val form = MongoShell("Open Command Terminal", "127.0.0.1", 27020)
             .useDatabase("transactions")
             .readDocumentsFromCollection("orders")
             .assertAllDocumentsHaveContracted(contractionVersion = 2)
             .build()
           form
        }

    16. Shutdown Midas
       ${
          val form = midasTerminal.stopMidas(27020)
          form
       }

    17. Clean up the database
       ${
          val form = MongoShell("Open MongoShell", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand("""db.dropDatabase()""")
            .build()
          form
       }

    18. Cleanup deltas directory
       ${
          contractionDelta.delete("Delete Delta File", "0001_removeFrom_transactions_orders_OrderListField.delta")
          expansionDelta1.delete("Delete Delta File", "0001_copy_transactions_orders_OrderListToCartsField.delta")
          expansionDelta2.delete("Delete Delta File", "0002_copy_transactions_orders_ZipcodeToPincodeField.delta")
          changeSetDir.delete("Delete ChangeSet Folder", "")
          appConfigFile.delete("Delete Application File", "incyWincyShoppingApp.midas")
          configFile.delete("Delete Deltas Directory", "midas.config")
       }

                                                                                    """
}



