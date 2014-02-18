package com.ee.midas.dsl.generator

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.dsl.Translator
import com.ee.midas.transform.{RequestTypes, ResponseTypes, TransformType, Transformer}
import com.ee.midas.dsl.interpreter.Reader
import java.io.{PrintWriter, File}
import java.util.ArrayList
import org.bson.BSONObject
import com.mongodb.util.JSON
import scala.collection.immutable.TreeMap
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.dsl.grammar.Verb
import com.ee.midas.dsl.interpreter.Parser
import groovy.lang.Closure
import java.util.regex.Pattern


@RunWith(classOf[JUnitRunner])
class ScalaGeneratorSpecs extends Specification with ResponseTypes with RequestTypes {

  val reader = new Reader
  val generator = new ScalaGenerator

  sequential
  "Scala Generator" should {
       "Generates Scala code for Add operation" in {
          //Given
          val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/add.delta")
          deltaFile.createNewFile()
          val expansionDelta = new PrintWriter(deltaFile)

          expansionDelta.write("use someDatabase\n")
          expansionDelta.write("db.collection.add(\'{\"newField\": \"newValue\"}\')\n")
          expansionDelta.flush()
          expansionDelta.close()
          val listDeltas = new ArrayList[File]()
          listDeltas.add(deltaFile)
          deltaFile.deleteOnExit()
          val tree = reader.read(listDeltas)


         //when
         val result = generator.generate(TransformType.EXPANSION, tree)

         //then
         var responseExpansions: Map[String, VersionedSnippets] =
             Map("someDatabase.collection" ->
           TreeMap(1.0d ->
             ((document: BSONObject) => {
               val json = "{\"newField\" : \"newValue\"}"
               val fields = JSON.parse(json).asInstanceOf[BSONObject]
               document ++ (fields, false)
             })
           ))
         var responseContractions: Map[String, VersionedSnippets] =
             Map()

         var requestExpansions: Map[ChangeSetCollectionKey, Double] =
             Map((1L, "someDatabase.collection") -> 1.0d)

         var requestContractions: Map[ChangeSetCollectionKey, Double] =
             Map()

         result.responseExpansions == responseExpansions
         result.responseContractions == responseContractions
         result.requestExpansions == requestExpansions
         result.requestContractions == requestContractions
       }

      "Generates Scala code for Remove operation" in {
        //Given
        val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/contraction/remove.delta")
        deltaFile.createNewFile()
        val expansionDelta = new PrintWriter(deltaFile)

        expansionDelta.write("use someDatabase\n")
        expansionDelta.write("db.collectionName.remove('[\"newField\"]')")
        expansionDelta.flush()
        expansionDelta.close()
        val listDeltas = new ArrayList[File]()
        listDeltas.add(deltaFile)
        deltaFile.deleteOnExit()
        val tree = reader.read(listDeltas)


        //when
        val result = generator.generate(TransformType.CONTRACTION, tree)

        //then
        var responseExpansions: Map[String, VersionedSnippets] =
          Map()

        var responseContractions: Map[String, VersionedSnippets] =
          Map("someDatabase.collectionName" ->
        TreeMap(1.0d ->
          ((document: BSONObject) => {
            val json = "[\"newField\"]"
            val fields = JSON.parse(json).asInstanceOf[BSONObject]
            document -- fields
          })
        ))
        var requestExpansions: Map[ChangeSetCollectionKey, Double] =
          Map()

        var requestContractions: Map[ChangeSetCollectionKey, Double] =
          Map((1L, "someDatabase.collectionName") -> 1.0d)

        result.responseExpansions == responseExpansions
        result.responseContractions == responseContractions
        result.requestExpansions == requestExpansions
        result.requestContractions == requestContractions
      }

      "Generates Scala code for Copy operation" in {
        //Given
        val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/copy.delta")
        deltaFile.createNewFile()
        val expansionDelta = new PrintWriter(deltaFile)

        expansionDelta.write("use someDatabase\n")
        expansionDelta.write("db.collectionName.copy(\"fromOldField\", \"toNewField\")")
        expansionDelta.flush()
        expansionDelta.close()
        val listDeltas = new ArrayList[File]()
        listDeltas.add(deltaFile)
        deltaFile.deleteOnExit()
        val tree = reader.read(listDeltas)


        //when
        val result = generator.generate(TransformType.EXPANSION, tree)

        //then
        var responseExpansions: Map[String, VersionedSnippets] =
          Map("someDatabase.collectionName" ->
              TreeMap(1.0d ->
                ((document: BSONObject) => {
                  document("fromOldField") match {
                  case Some(fromFieldValue) => document("toNewField") = fromFieldValue
                  case None => document
                }
            })
          ))

        var responseContractions: Map[String, VersionedSnippets] =
          Map()

        var requestExpansions: Map[ChangeSetCollectionKey, Double] =
          Map((1L, "someDatabase.collectionName") -> 1.0d)

        var requestContractions: Map[ChangeSetCollectionKey, Double] =
          Map()

        result.responseExpansions == responseExpansions
        result.responseContractions == responseContractions
        result.requestExpansions == requestExpansions
        result.requestContractions == requestContractions
      }

      "Generates Scala code for split operation" in {
        //Given
        val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/split.delta")
        deltaFile.createNewFile()
        val expansionDelta = new PrintWriter(deltaFile)

        expansionDelta.write("use someDatabase\n")
        expansionDelta.write("db.collectionName.split(\"sourceField\", \"some regex\", \"{ \\\"token1\\\": \\\"\\$1\\\", \\\"token2\\\": \\\"\\$2\\\"}\")")
        expansionDelta.flush()
        expansionDelta.close()
        val listDeltas = new ArrayList[File]()
        listDeltas.add(deltaFile)
        deltaFile.deleteOnExit()
        val tree = reader.read(listDeltas)


        //when
        val result = generator.generate(TransformType.EXPANSION, tree)

        //then
        var responseExpansions: Map[String, VersionedSnippets] =
        Map("someDatabase.collectionName" ->
        TreeMap(1.0d ->
          ((document: BSONObject) => document <~> ("sourceField", Pattern.compile("someregex"), "{\"token1\": \"$1\", \"token2\": \"$2\" }"))
        ))

        var responseContractions: Map[String, VersionedSnippets] =
          Map()

        var requestExpansions: Map[ChangeSetCollectionKey, Double] =
          Map((1L, "someDatabase.collectionName") -> 1.0d)

        var requestContractions: Map[ChangeSetCollectionKey, Double] =
          Map()

        result.responseExpansions == responseExpansions
        result.responseContractions == responseContractions
        result.requestExpansions == requestExpansions
        result.requestContractions == requestContractions
      }

    "Generates Scala code for Merge operation" in {
      //Given
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/merge.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase\n")
      expansionDelta.write("db.collectionName.merge(\"[\\\"field1\\\",\\\"field2\\\"]\", \"separator\", \"targetField\")")
      expansionDelta.flush()
      expansionDelta.close()
      val listDeltas = new ArrayList[File]()
      listDeltas.add(deltaFile)
      deltaFile.deleteOnExit()
      val tree = reader.read(listDeltas)


      //when
      val result = generator.generate(TransformType.EXPANSION, tree)

      //then
      var responseExpansions: Map[String, VersionedSnippets] =
        Map("someDatabase.collectionName" ->
          TreeMap(1.0d ->
            ((document: BSONObject) => {
              val fields = List("field1","field2")
              document >~< ("targetField", "separator", fields)
            })
        ))

      var responseContractions: Map[String, VersionedSnippets] =
        Map()

      var requestExpansions: Map[ChangeSetCollectionKey, Double] =
        Map((1L, "someDatabase.collectionName") -> 1.0d)

      var requestContractions: Map[ChangeSetCollectionKey, Double] =
        Map()

      result.responseExpansions == responseExpansions
      result.responseContractions == responseContractions
      result.requestExpansions == requestExpansions
      result.requestContractions == requestContractions
    }

    "Generates empty Scala expansion response map for expansion delta in contraction mode" in {
      //Given
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/add.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase\n")
      expansionDelta.write("db.collectionName.add(\"{\\\"field1\\\": \\\"value1\\\",\\\"field2\\\": \\\"value2\\\"}\")")
      expansionDelta.flush()
      expansionDelta.close()
      val listDeltas = new ArrayList[File]()
      listDeltas.add(deltaFile)
      deltaFile.deleteOnExit()
      val tree = reader.read(listDeltas)


      //when
      val result = generator.generate(TransformType.CONTRACTION, tree)

      //then
      var responseExpansions: Map[String, VersionedSnippets] =
        Map()

      var responseContractions: Map[String, VersionedSnippets] =
        Map()

      var requestExpansions: Map[ChangeSetCollectionKey, Double] =
        Map()

      var requestContractions: Map[ChangeSetCollectionKey, Double] =
        Map()

      result.responseExpansions == responseExpansions
      result.responseContractions == responseContractions
      result.requestExpansions == requestExpansions
      result.requestContractions == requestContractions
    }

    "Generates empty Scala maps for contraction delta in expansion mode" in {
      //Given
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/contraction/remove.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase\n")
      expansionDelta.write("db.collectionName.remove(\"[\\\"field1\\\",\\\"field2\\\"]\")")
      expansionDelta.flush()
      expansionDelta.close()
      val listDeltas = new ArrayList[File]()
      listDeltas.add(deltaFile)
      deltaFile.deleteOnExit()
      val tree = reader.read(listDeltas)


      //when
      val result = generator.generate(TransformType.EXPANSION, tree)

      //then
      var responseExpansions: Map[String, VersionedSnippets] =
        Map()

      var responseContractions: Map[String, VersionedSnippets] =
        Map()

      var requestExpansions: Map[ChangeSetCollectionKey, Double] =
        Map()

      var requestContractions: Map[ChangeSetCollectionKey, Double] =
        Map()

      result.responseExpansions == responseExpansions
      result.responseContractions == responseContractions
      result.requestExpansions == requestExpansions
      result.requestContractions == requestContractions
    }
  }

}
