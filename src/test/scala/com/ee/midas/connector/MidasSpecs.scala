package com.ee.midas.connector

import org.specs2.mutable.{Before, After, Specification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.mongodb._

import com.mongodb.MongoException.DuplicateKey

// TODO : Write specs for connection failure scenaro

@RunWith(classOf[JUnitRunner])
object MidasSpecs extends Specification {

     var application: MongoClient = null
    "midas" should {
       "bubble up mongo internal exceptions to the client" in new tests{
//         Midas.main(Array("localhost","27020","localhost","27017"))
         application = new MongoClient("localhost", 27020)
         val database:DB = application.getDB("midas")
         val collection:DBCollection = database.getCollection("demo1k")
         val document: DBObject = collection.findOne()
         collection.insert(document) must throwA[DuplicateKey]
       }
    }

  trait tests extends After {
    def after = application.close()
  }
}
