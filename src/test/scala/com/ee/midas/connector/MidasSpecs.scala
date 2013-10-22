package com.ee.midas.connector

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.mongodb._

import com.mongodb.MongoException.DuplicateKey

@RunWith(classOf[JUnitRunner])
object MidasSpecs extends Specification {
    "midas" should {
       "bubble up mongo internal exceptions to the client" in {
          val midas:Midas = new Midas()
          midas.start("localhost",27020,"localhost",27017)
          val application: MongoClient = new MongoClient("localhost", 27020)
          val database:DB = application.getDB("midas")
          val collection:DBCollection = database.getCollection("demo1k")
          val document: DBObject = collection.findOne()
          collection.insert(document) must throwAn[DuplicateKey]
       }
    }

}
