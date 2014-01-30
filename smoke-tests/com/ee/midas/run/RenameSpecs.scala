package com.ee.midas.run

import org.specs2._
import specification._
import com.ee.midas.fixtures.{Delta, CommandTerminal, MongoShell}

class RenameSpecs extends Specification with Forms {
  sequential
  var midasTerminal = CommandTerminal("")
  var expansionDelta1: Delta = null
  var expansionDelta2: Delta = null
  var contractionDelta: Delta = null

  def is = s2"""
    ${"Rename Operation".title}
    Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. Bob, the Business analyst
               wants to rename certain things. He approaches Dave, the Developer.

    Bob:  "Hey Dave, Lets rename zip to pin in address because it is closer to the domain.
           Also, Let's rename Order List as Your Cart"
    Dave: "Ya true Bob, I agree with you."
    Bob:  "How do you plan to do that Dave ? Do we need to have some downtime ?"
    Dave: "No Bob, We will use Midas which will migrate our schema on the fly. Here is what we can do
           for zero downtime deployment. First, we will run Expansion scripts and copy the current
           field to new field. This will keep our application backwards compatible with the existing
           version."
    Bob:  "Oh.. So Expansion will add the new field and keep the old field as well."
    Dave: "Yes exactly."
    Bob:  "Okay, but we have 2 nodes in our cluster. So, Do we apply this to all nodes
           simultaneously."
    Dave: "No, This upgradation will be done for one node of a cluster at a time. Once the system is
           completely upgraded and deemed stable, we will run the contraction scripts and remove the
           old field"
    Bob:  "Okay, but what if after adding new field system is not stable . Do we need to rollback DB?"
    Dave: "No Bob. DB Rollback can lead to loss in data and leave database in inconsistent state.
           In that case it will be better to rollback application instead."
    Bob:  "Oh ... right . That makes sense"
    Dave: "So after the Expansion - Contraction cycle the system will be migrated completely."
    Bob:  "Thanks, that sounds good."

    1. Let's assume we have following documents in the database. We simulate this by inserting
       documents in the database .
      ${
          val form = MongoShell("Open Mongo Shell", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand(s"""db.orders.insert({name:"Vivek", "OrderList": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411006} })""")
            .runCommand(s"""db.orders.insert({name:"Komal", "OrderList": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411004} })""")
            .runCommand(s"""db.orders.insert({name:"Dhaval", "OrderList": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 110007} })""")
            .retrieve()
          form
      }

    2. Create delta file "0001_copy_transactions_orders_OrderListToYourCartField.delta" to copy "OrderList"
       into "YourCart" at location "deltaSpecs" in "expansion" folder
      ${
          val baseDeltaDir = "/deltaSpecs"
          expansionDelta1 = Delta(baseDeltaDir, "EXPANSION", () => {
            """use transactions
               db.orders.copy('OrderList','YourCart')
            """
          } )
          val form = expansionDelta1.saveAs("0001_copy_transactions_orders_OrderListToYourCartField.delta")
          form
      }

    3. Create delta file "0002_copy_transactions_orders_ZipcodeToPincodeField.delta" to copy "zipcode"
       to "pincode" at location "deltaSpecs" in "expansion" folder
      ${
          val baseDeltaDir = "/deltaSpecs"
          expansionDelta2 = Delta(baseDeltaDir, "EXPANSION", () => {
            """use transactions
               db.orders.copy("ShippingAddress.zipcode", "ShippingAddress.pincode")
            """
          } )
          val form = expansionDelta2.saveAs("0002_copy_transactions_orders_ZipcodeToPincodeField.delta")
          form
        }

    4. Start Midas in EXPANSION mode
      ${
          midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "EXPANSION")
          val form = midasTerminal.startMidas
          form
      }

    5. Connect with midas and verify that read documents contain new fields "YourCart" and "ShippingAddress.pincode"
      ${
          val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "localhost", 27020)
            .useDatabase("transactions")
            .verifyIfCopied("orders", Array(("YourCart", "OrderList"), ("ShippingAddress.pincode","ShippingAddress.zipcode")))
            .retrieve()
          form
      }

    6. WebApp update and write back the documents to database
      ${
          val form =  MongoShell("IncyWincyShoppingApp - UpgradedVersion", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand("""db.orders.update({name: "Vivek"}, { $set: {"YourCart": ['shoes', 'sipper'], "ShippingAddress.pincode": 411006, "_expansionVersion": 2}}, {$upsert: true, multi: true})""")
            .runCommand("""db.orders.update({name: "Komal"}, { $set: {"YourCart": ['scarf', 'footwear'], "ShippingAddress.pincode": 411004, "_expansionVersion": 2}}, {$upsert: true, multi: true})""")
            .runCommand("""db.orders.update({name: "Dhaval"}, { $set: {"YourCart": ['headsets'], "ShippingAddress.pincode": 110007, "_expansionVersion": 2}}, {$upsert: true, multi: true})""")
            .retrieve()
          form
      }

    7. Create delta file "0001_removeFrom_transactions_orders_OrderListField.delta" to remove "age"
       and "ShippingAddress.zipcode" at location "deltaSpecs" in "contraction" folder
      ${
          val baseDeltaDir = "/deltaSpecs"
          contractionDelta = Delta(baseDeltaDir, "CONTRACTION", () => {
            """use transactions
               db.orders.remove("['OrderList']")
               db.orders.remove("['ShippingAddress.zipcode']")
            """
          } )
          val form = contractionDelta.saveAs("0001_removeFrom_transactions_orders_OrderListField.delta")
          form
      }

    8. Restart Midas in CONTRACTION mode
      ${
          val form = midasTerminal.stopMidas(27020)
          form
       }

      ${
          midasTerminal = CommandTerminal("--port", "27040", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "CONTRACTION")
          val form = midasTerminal.startMidas
          form
      }

    9. Connect with midas and verify that "OrderList" and "ShippingAddress.zipcode" fields are removed from documents
      ${
          val form = MongoShell("Open Command Terminal", "localhost", 27040)
            .useDatabase("transactions")
            .verifyIfRemoved("orders", Array("OrderList", "ShippingAddress.zipcode"))
            .retrieve()
          form
      }

    10. Clean up the database
      ${
          val form = MongoShell("Open MongoShell", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand(s"""db.dropDatabase()""")
            .retrieve()
          form
      }

    11. Cleanup deltas directory
      ${
          expansionDelta1.delete("0001_copy_transactions_orders_OrderListToYourCartField.delta")
          expansionDelta2.delete("0002_copy_transactions_orders_ZipcodeToPincodeField.delta")
          contractionDelta.delete("0001_removeFrom_transactions_orders_OrderListField.delta")
      }

    12. Shutdown Midas
      ${
          val form = midasTerminal.stopMidas(27040)
          form
       }
                                                                                                   """
}


