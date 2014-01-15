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
          runCommand(s"""db.orders.insert({name:"Vivek", "OrderList": ['shoes', 'sipper'], "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", zipcode: 411006} })""").
          runCommand(s"""db.orders.insert({name:"Komal", "OrderList": ['scarf', 'footwear'], "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", zipcode: 411006} })""").
          runCommand(s"""db.orders.insert({name:"Dhaval", "OrderList": ['headsets'], "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", zipcode: 110007} })""").
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

    4. Connect with midas and verify that read documents contain "DOB" field
      ${  val form = MongoShell("IncyWincyShoppingApp UpgradedVersion", "localhost", 27020).useDatabase("transactions").
          copied("orders", "YourCart", "OrderList").
          retrieve()
          form
      }

    5. Update and write back the documents to the database
      ${  val form =  MongoShell("IncyWincyShoppingApp UpgradedVersion", "localhost", 27017).useDatabase("transactions").
          runCommand("""db.orders.update({name: "Vivek"}, { $set: {"YourCart": ['shoes', 'sipper'], "_expansionVersion": 1}}, {$upsert: true, multi: true})""").
          runCommand("""db.orders.update({name: "Komal"}, { $set: {"YourCart": ['shoes', 'sipper'], "_expansionVersion": 1}}, {$upsert: true, multi: true})""").
          runCommand("""db.orders.update({name: "Dhaval"}, { $set: {"YourCart": ['shoes', 'sipper'], "_expansionVersion": 1}}, {$upsert: true, multi: true})""").
          retrieve()
          form
      }

    6. Create delta file "0001_removeFrom_transactions_orders_OrderListField.delta" to remove "age" at
       location "deltaSpecs" in "contraction" folder
      ${  val baseDeltaDir = "/deltaSpecs"
          contractionDelta = Delta(baseDeltaDir, "CONTRACTION", () => {
            """use transactions
               db.orders.remove("['OrderList']")
            """
          } )
          val form = contractionDelta.saveAs("0001_removeFrom_transactions_orders_OrderListField.delta")
          form
      }


    7. Restart Midas in CONTRACTION mode
      ${ midasTerminal.stopMidas(27020)
       }
      ${  midasTerminal = CommandTerminal("--port", "27040", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "CONTRACTION")
          val form = midasTerminal.startMidas
          form
      }

    8. Connect with midas and verify that read documents do not contain "OrderList" field
      ${  val form = MongoShell("Open Command Terminal", "localhost", 27040).useDatabase("transactions").
          removed("orders", "OrderList").
          retrieve()
          form
      }

    9. Clean up the database
      ${  val form = MongoShell("Open MongoShell", "localhost", 27017).
          useDatabase("transactions").
          runCommand(s"""db.dropDatabase()""").
          retrieve()
          form
      }

    10. Cleanup Deltas Directory
      ${ expansionDelta1.delete("0001_copy_transactions_orders_OrderListToYourCartField.delta")
         contractionDelta.delete("0001_removeFrom_transactions_orders_OrderListField.delta")
      }

    11:Shutdown Midas            ${ midasTerminal.stopMidas(27040)}
                                                                                                   """
}


