package com.ee.midas.run

import org.specs2._
import specification._
import com.ee.midas.fixtures.{Delta, CommandTerminal, MongoShell}

class Transform extends Specification with Forms {
  sequential
  var midasTerminal = CommandTerminal("")
  var expansionDelta1: Delta = null
  def is = s2"""
    ${"Transform Operation".title}
    Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. Bob, the Business analyst
               wants certain schema changes. He approaches Dave, the Developer.

    Bob:  "Dave, I was thinking about certain changes and would like to discuss that with you."
    Dave: "Ya sure Bob"
    Bob:  "I want to display user's name along with title on their home page like Mr. John."
    Dave: "Ok, So you want add title to the schema and merge it with name."
    Bob:  "Yes exactly. Also lets show the amount customer is saving on each purchase."
    Bob adding further: "Lets append +91 to mobile numbers."
    Dave: "Okay, We will run Expansion scripts for all these transformations and upgrade the schema
           gradually."

    1. Let's assume we have following documents in the database. We simulate this by inserting
       documents in the database .
      ${
          val form = MongoShell("Open Mongo Shell", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand(s"""db.orders.insert({name:"Vivek", "OrderList": ['shoes', 'sipper'], "Discount": 0, "TotalAmount": 6000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411006}, "MobileNo": "1234567891" })""")
            .runCommand(s"""db.orders.insert({name:"Komal", "OrderList": ['scarf', 'footwear'], "Discount": 40, "TotalAmount": 3000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 411004}, "MobileNo": "1234123412" })""")
            .runCommand(s"""db.orders.insert({name:"Dhaval", "OrderList": ['headsets'], "Discount": 30, "TotalAmount": 8000, ShippingAddress: {line1: "enter house/street", line2: "enter city", "zipcode": 110007}, "MobileNo": "1111111111" })""")
            .retrieve()
          form
        }

     2. Create deltas directory with two folders "expansion" and "contraction".
      ${
         val baseDeltaDir = "/deltaSpecs"
         val delta = Delta(baseDeltaDir, "EXPANSION", () => "")
         true
       }
     2. Start Midas in EXPANSION mode
      ${
          midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "EXPANSION")
          val form = midasTerminal.startMidas
          form
        }

    3. Create delta file "0001_transactions_orders.delta" to perform transformations on "orders" at
       location "deltaSpecs" in "expansion" folder
      ${
          val baseDeltaDir = "/deltaSpecs"
          expansionDelta1 = Delta(baseDeltaDir, "EXPANSION", () => {
            """use transactions
               db.orders.add('{"Title": "Miss./Mr."}')
               db.orders.mergeInto('NameWithTitle',' ','["Title", "name"]' )
               db.orders.transform("Saving", "{$multiply: [1000,10]}")
            """
          } )
          val form = expansionDelta1.saveAs("0001_transactions_orders.delta")
          form
        }

    4. Connect with midas and verify that read documents contain all transformations.
      ${
          val form = MongoShell("IncyWincyShoppingApp - UpgradedVersion", "localhost", 27020)
            .useDatabase("transactions")
            .verifyIfAdded("orders", Array("Title", "NamewithTitle", "Saving"))
            .retrieve()
          form
        }

    5. Cleanup deltas directory
      ${
          expansionDelta1.delete("0001_copy_transactions_orders_OrderListToYourCartField.delta")
       }
    6. Clean up the database
      ${
          val form = MongoShell("Open MongoShell", "localhost", 27017)
            .useDatabase("transactions")
            .runCommand(s"""db.dropDatabase()""")
            .retrieve()
          form
        }


    7. Shutdown Midas
      ${
          val form = midasTerminal.stopMidas(27020)
          form
        }

  """
}
