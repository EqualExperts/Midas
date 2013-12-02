package com.ee.midas.transform

import org.bson.BSONObject
import com.mongodb.util.JSON
import DocumentOperations._

object Transformations extends Transforms {
  //  WARNING: DO NOT WRITE ANYTHING IN THIS FILE, IT IS REGENRATED AT RUNTIME!!
//  override lazy val expansions: Map[String, VersionedSnippets] = Map()
//  override lazy val contractions: Map[String, VersionedSnippets] = Map()
  override lazy val expansions: Map[String, VersionedSnippets] =
    Map("users.customers" ->
      Map(1d ->
        ((document: BSONObject) => {
          val json = """{"age" : 0 }"""
          val fields = JSON.parse(json).asInstanceOf[BSONObject]
          document ++ fields
        })
        , 2d ->
          ((document: BSONObject) => {
            val json = """{"city" : "Please Set City" }"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document ++ fields
          })
      ), "transactions.orders" ->
      Map(1d ->
        ((document: BSONObject) => {
          val json = """{ "executionDate" : "new Date('Jun 23, 1912')" }"""
          val fields = JSON.parse(json).asInstanceOf[BSONObject]
          document ++ fields
        })
        , 2d ->
          ((document: BSONObject) => {
            val json = """{"dispatchDate" : "Some default"}"""
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document ++ fields
          })
      ))

  override lazy val contractions: Map[String, VersionedSnippets] =
    Map("users.customers" ->
      Map(1d ->
        ((document: BSONObject) => {
          val json = """[ "age"]"""
          val fields = JSON.parse(json).asInstanceOf[BSONObject]
          document -- fields
        })
      ), "transactions.orders" ->
      Map())

}


