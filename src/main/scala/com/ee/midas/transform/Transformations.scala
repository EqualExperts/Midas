package com.ee.midas.transform

import com.mongodb.util.JSON
import org.bson.BSONObject
import com.ee.midas.transform.DocumentOperations._

//object Transformations extends Transforms {
//  WARNING: DO NOT WRITE ANYTHING IN THIS FILE, IT IS REGENRATED AT RUNTIME!!
//  override lazy val expansions = ???
//  override lazy val contractions = ???
//}

// WARNING: THIS FILE IS AUTO-GENERATED, DO NOT HAND-CODE HERE, IT WILL BE OVER-WRITTEN
// UPON NEXT RE-GENERATION.
object Transformations extends Transforms {


  val transactions_orders_1_add = (document: BSONObject) => {
    val json = """{ "executionDate" : "new Date('Jun 23, 1912')" }"""
    val fields = JSON.parse(json).asInstanceOf[BSONObject]
    document ++ fields
  }


  override lazy val expansions =
    transactions_orders_1_add :: Nil


  val users_customers_1_remove = (document: BSONObject) => {
    val json = """["age"]"""
    val fields = JSON.parse(json).asInstanceOf[BSONObject]
    document -- fields
  }


  val users_customers_2_remove = (document: BSONObject) => {
    val json = """[ "name"]"""
    val fields = JSON.parse(json).asInstanceOf[BSONObject]
    document -- fields
  }


  override lazy val contractions =

    users_customers_1_remove :: users_customers_2_remove :: Nil

}
