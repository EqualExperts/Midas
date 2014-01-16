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
    Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. Bob, the Business analyst
               wants certain changes in the names of the terminology used . So he approches Dave,
               the Developer.

    Bob: " Hey Dave, Let's call Order List as Your Cart because it is more closer to the domain.
           Users will feel more comfortable with this terminology. Also , Lets change zipcode
           in address to pincode"
    Dave: " Ya true Bob, I also feel the same . We will copy Order List field to Your Cart in
            the schema and slowly bring all the orders documents to same consistent state"
    Dave adding further: " After that , we will remove the Order List field.
                           And will do the same for pincode and zipcode "
    Bob: " Thanks, that will be great"

    1. Insert documents in the database .
      ${  val form = MongoShell("Open MongoShell", "localhost", 27017).useDatabase("transactions").
          runCommand(s"""db.orders.insert({name:"Vivek", "OrderList": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411006} })""").
          runCommand(s"""db.orders.insert({name:"Komal", "OrderList": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411004} })""").
          runCommand(s"""db.orders.insert({name:"Dhaval", "OrderList": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 110007} })""").
          retrieve()
          form
      }

    2. Create delta file "0001_copy_transactions_orders_OrderListToYourCartField.delta" to copy "OrderList"
       to "YourCart" at location "deltaSpecs" in "expansion" folder
      ${  val baseDeltaDir = "/deltaSpecs"
          expansionDelta1 = Delta(baseDeltaDir, "EXPANSION", () => {
            """use transactions
               db.orders.copy('OrderList','YourCart')
            """
          } )
          val form = expansionDelta1.saveAs("0001_copy_transactions_orders_OrderListToYourCartField.delta")
          form
      }

    3. Start Midas in EXPANSION mode
      ${  midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "EXPANSION")
          val form = midasTerminal.startMidas
          form
      }

    4. Create delta file "0002_copy_transactions_orders_ZipcodeToPincodeField.delta" to copy "zipcode"
       to "pincode" at location "deltaSpecs" in "expansion" folder
      ${  val baseDeltaDir = "/deltaSpecs"
          expansionDelta2 = Delta(baseDeltaDir, "EXPANSION", () => {
            """use transactions
               db.orders.copy("ShippingAddress.zipcode", "ShippingAddress.pincode")
            """
          } )
          val form = expansionDelta2.saveAs("0002_copy_transactions_orders_ZipcodeToPincodeField.delta")
          form
      }

    5. Connect with midas and verify that read documents contain "Your Cart" and "ShippingAddress.pincode" field
      ${  val form = MongoShell("IncyWincyShoppingApp UpgradedVersion", "localhost", 27020).useDatabase("transactions").
          copied("orders", Array(("YourCart", "OrderList"), ("ShippingAddress.pincode","ShippingAddress.zipcode"))).
          retrieve()
          form
      }

    6. Update and write back the documents to the database
      ${  val form =  MongoShell("IncyWincyShoppingApp UpgradedVersion", "localhost", 27017).useDatabase("transactions").
          runCommand("""db.orders.update({name: "Vivek"}, { $set: {"YourCart": ['shoes', 'sipper'], "ShippingAddress.pincode": 411006, "_expansionVersion": 2}}, {$upsert: true, multi: true})""").
          runCommand("""db.orders.update({name: "Komal"}, { $set: {"YourCart": ['scarf', 'footwear'], "ShippingAddress.pincode": 411004, "_expansionVersion": 2}}, {$upsert: true, multi: true})""").
          runCommand("""db.orders.update({name: "Dhaval"}, { $set: {"YourCart": ['headsets'], "ShippingAddress.pincode": 110007, "_expansionVersion": 2}}, {$upsert: true, multi: true})""").
          retrieve()
          form
      }

    7. Create delta file "0001_removeFrom_transactions_orders_OrderListField.delta" to remove "age"
       and "ShippingAddress.zipcode" at location "deltaSpecs" in "contraction" folder
      ${  val baseDeltaDir = "/deltaSpecs"
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
      ${ val form = midasTerminal.stopMidas(27020)
         form
       }
      ${  midasTerminal = CommandTerminal("--port", "27040", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "CONTRACTION")
          val form = midasTerminal.startMidas
          form
      }

    9. Connect with midas and verify that read documents do not contain "OrderList" and "ShippingAddress.zipcode" field
      ${  val form = MongoShell("Open Command Terminal", "localhost", 27040).useDatabase("transactions").
          removed("orders", Array("OrderList", "ShippingAddress.zipcode")).
          retrieve()
          form
      }

    10. Clean up the database
      ${  val form = MongoShell("Open MongoShell", "localhost", 27017).
          useDatabase("transactions").
          runCommand(s"""db.dropDatabase()""").
          retrieve()
          form
      }

    11. Cleanup Deltas Directory
      ${  expansionDelta1.delete("0001_copy_transactions_orders_OrderListToYourCartField.delta")
          expansionDelta2.delete("0002_copy_transactions_orders_ZipcodeToPincodeField.delta")
          contractionDelta.delete("0001_removeFrom_transactions_orders_OrderListField.delta")
      }

    12. Shutdown Midas
      ${  val form = midasTerminal.stopMidas(27040)
          form
       }
                                                                                                   """
}


