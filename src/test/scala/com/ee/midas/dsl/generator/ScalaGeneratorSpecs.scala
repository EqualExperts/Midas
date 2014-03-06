/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.dsl.generator

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.transform.{RequestTypes, ResponseTypes, TransformType}
import com.ee.midas.dsl.interpreter.Reader
import java.io.{FileWriter, PrintWriter, File}
import java.util.ArrayList
import org.bson.BSONObject
import com.mongodb.util.JSON
import scala.collection.immutable.TreeMap
import com.ee.midas.transform.DocumentOperations._
import java.util.regex.Pattern
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ScalaGeneratorSpecs extends Specification with ResponseTypes with RequestTypes {

  val NEW_LINE = System.getProperty("line.separator")

  trait Setup extends Scope {
    val reader = new Reader
    val generator = new ScalaGenerator
  }

  sequential
  "Scala Generator" should {
       "Generates Scala code for Add operation" in new Setup {
          //Given
          val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/add.delta")
          deltaFile.createNewFile()
          val expansionDelta = new FileWriter(deltaFile)


          expansionDelta.write("use someDatabase")
          expansionDelta.write(NEW_LINE)
          expansionDelta.write(s"""db.collection.add('{"newField": "newValue"}')""")
          expansionDelta.write(NEW_LINE)
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
           TreeMap(1.0 ->
             ((document: BSONObject) => {
               val json = "{\"newField\" : \"newValue\"}"
               val fields = JSON.parse(json).asInstanceOf[BSONObject]
               document ++ (fields, false)
             })
           ))
         var responseContractions: Map[String, VersionedSnippets] =
             Map()

         var requestExpansions: Map[ChangeSetCollectionKey, Double] =
             Map((1L, "someDatabase.collection") -> 1.0)

         var requestContractions: Map[ChangeSetCollectionKey, Double] =
             Map()

         result.responseExpansions.toString mustEqual responseExpansions.toString
         result.responseContractions mustEqual responseContractions
         result.requestExpansions mustEqual requestExpansions
         result.requestContractions mustEqual requestContractions
       }

      "Generates Scala code for Remove operation" in new Setup {
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

        result.responseExpansions mustEqual responseExpansions
        result.responseContractions.toString mustEqual responseContractions.toString
        result.requestExpansions mustEqual requestExpansions
        result.requestContractions mustEqual requestContractions
      }

      "Generates Scala code for Copy operation" in new Setup {
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

        result.responseExpansions.toString mustEqual responseExpansions.toString
        result.responseContractions mustEqual responseContractions
        result.requestExpansions mustEqual requestExpansions
        result.requestContractions mustEqual requestContractions
      }

      "Generate Scala code for split operation" in new Setup {
        //Given
        val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/split.delta")
        deltaFile.createNewFile()
        val expansionDelta = new PrintWriter(deltaFile)

        expansionDelta.write("use someDatabase")
        expansionDelta.write(NEW_LINE)
        expansionDelta.write("""db.collectionName.split("sourceField", "some regex", "{ 'token1' : '\$1', token2: '\$2'}")""")
        expansionDelta.write(NEW_LINE)
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
          ((document: BSONObject) => document <~> ("sourceField", Pattern.compile("some regex"), "{\"token1\": \"$1\", \"token2\": \"$2\" }"))
        ))

        var responseContractions: Map[String, VersionedSnippets] =
          Map()

        var requestExpansions: Map[ChangeSetCollectionKey, Double] =
          Map((1L, "someDatabase.collectionName") -> 1.0d)

        var requestContractions: Map[ChangeSetCollectionKey, Double] =
          Map()

        result.responseExpansions.toString mustEqual responseExpansions.toString
        result.responseContractions mustEqual responseContractions
        result.requestExpansions mustEqual requestExpansions
        result.requestContractions mustEqual requestContractions
      }

    "Generate Scala code for Merge operation" in new Setup {
      //Given
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/merge.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase")
      expansionDelta.write(NEW_LINE)
      expansionDelta.write("db.collectionName.merge('[\"field1\",\"field2\"]', \"separator\", \"targetField\")")
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

      result.responseExpansions.toString mustEqual responseExpansions.toString
      result.responseContractions mustEqual responseContractions
      result.requestExpansions mustEqual requestExpansions
      result.requestContractions mustEqual requestContractions
    }

    "Generates empty Scala expansion response map for expansion delta in contraction mode" in new Setup {
      //Given
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/expansion/add2.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase")
      expansionDelta.write(NEW_LINE)
      expansionDelta.write("db.collectionName.add('{\"field1\": \"value1\",\"field2\": \"value2\"}')")
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
        Map("someDatabase.collectionName" -> TreeMap[Double, Snippet]())

      var requestExpansions: Map[ChangeSetCollectionKey, Double] =
        Map((1L, "someDatabase.collectionName") -> 1.0)

      var requestContractions: Map[ChangeSetCollectionKey, Double] =
        Map()

      result.responseExpansions mustEqual responseExpansions
      result.responseContractions mustEqual responseContractions
      result.requestExpansions mustEqual requestExpansions
      result.requestContractions mustEqual requestContractions
    }

    "Generates empty Scala maps for contraction delta in expansion mode" in new Setup {
      //Given
      val deltaFile = new File("src/test/scala/com/ee/midas/myDeltas/myApp/001-ChangeSet/contraction/remove2.delta")
      deltaFile.createNewFile()
      val expansionDelta = new PrintWriter(deltaFile)

      expansionDelta.write("use someDatabase")
      expansionDelta.write(NEW_LINE)
      expansionDelta.write("db.collectionName.remove('[\"field1\",\"field2\"]')")
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
        Map("someDatabase.collectionName" -> TreeMap[Double, Snippet]())

      var responseContractions: Map[String, VersionedSnippets] =
        Map()

      var requestExpansions: Map[ChangeSetCollectionKey, Double] =
        Map()

      var requestContractions: Map[ChangeSetCollectionKey, Double] =
        Map()

      result.responseExpansions mustEqual responseExpansions
      result.responseContractions mustEqual responseContractions
      result.requestExpansions mustEqual requestExpansions
      result.requestContractions mustEqual requestContractions
    }
  }

}
