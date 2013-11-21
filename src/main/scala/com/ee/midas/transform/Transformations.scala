package com.ee.midas.transform

import com.mongodb.util.JSON
import org.bson.BSONObject
import com.ee.midas.transform.DocumentOperations._

//object Transformations extends Transforms {
//  WARNING: DO NOT WRITE ANYTHING IN THIS FILE, IT IS REGENRATED AT RUNTIME!!
//  override lazy val expansions: Map[String, VersionedSnippets] = ???
//  override lazy val contractions: Map[String, VersionedSnippets] = ???
//}

object Transformations extends Transforms {

  override lazy val expansions: Map[String, VersionedSnippets] =
    Map("users.customers" ->
      Map(1 ->
        ((document: BSONObject) => {
          val json = """{"age" : 0 }"""
          val fields = JSON.parse(json).asInstanceOf[BSONObject]
          document ++ fields
        })
        , 2 ->
          ((document: BSONObject) => {
            val json = """{"city" : "Please Set City" }"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document ++ fields
          })
      ), "transactions.orders" ->
      Map(1 ->
        ((document: BSONObject) => {
          val json = """{ "executionDate" : "new Date('Jun 23, 1912')" }"""
          val fields = JSON.parse(json).asInstanceOf[BSONObject]
          document ++ fields
        })
      ))

  override lazy val contractions: Map[String, VersionedSnippets] =
    Map("users.customers" ->
      Map(1 ->
        ((document: BSONObject) => {
          val json = """[ "age"]"""
          val fields = JSON.parse(json).asInstanceOf[BSONObject]
          document -- fields
        })
      ), "transactions.orders" ->
      Map())

}
