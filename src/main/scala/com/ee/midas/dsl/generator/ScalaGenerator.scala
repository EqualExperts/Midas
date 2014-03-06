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

import com.ee.midas.transform.{TransformType, Transformer}
import com.ee.midas.dsl.interpreter.representation.Tree
import com.ee.midas.utils.Loggable
import com.ee.midas.dsl.grammar.Verb
import scala.collection.JavaConverters._
import org.bson.BSONObject
import scala.collection.immutable.TreeMap
import com.ee.midas.transform.TransformType._

class ScalaGenerator extends Generator[Transformer] with Loggable with SnippetProvider {
  type JDouble = java.lang.Double
  type JLong = java.lang.Long
  type JList[String] = java.util.List[String]
  type MutableVersionedMap = scala.collection.mutable.Map[JDouble, (Verb, JList[String], JLong)]

  def generate(transformType: TransformType, tree: Tree): Transformer = transformType match {
    case EXPANSION => new Transformer {
      var responseExpansions: Map[String, VersionedSnippets] = generateResponseSnippets(EXPANSION, tree)
      var responseContractions: Map[String, VersionedSnippets] = Map()
      var transformType: TransformType = EXPANSION
      var requestExpansions: Map[ChangeSetCollectionKey, Double] = generateRequestTransforms(EXPANSION, tree)
      var requestContractions: Map[ChangeSetCollectionKey, Double] = Map()
    }

    case CONTRACTION => new Transformer {
      var responseExpansions: Map[String, VersionedSnippets] = Map()
      var responseContractions: Map[String, VersionedSnippets] = generateResponseSnippets(CONTRACTION, tree)
      var transformType: TransformType = CONTRACTION
      var requestExpansions: Map[ChangeSetCollectionKey, Double] = generateRequestTransforms(EXPANSION, tree)
      var requestContractions: Map[ChangeSetCollectionKey, Double] = generateRequestTransforms(CONTRACTION, tree)
    }
  }

  private def fullCollectionName(dbName: String, collectionName: String) =
    s"$dbName.$collectionName"

  private def generateRequestTransforms(transformType: TransformType, tree: Tree):
  Map[(Long, String), Double] = {
    logInfo(s"Started Request Transforms generation for $transformType TransformType...")
    val requestTransforms = scala.collection.mutable.Map[(Long, String), Double]()
    tree.foreachDelta(transformType, { case (dbName: String, collectionName: String, versionedMap: MutableVersionedMap) =>
      val changeSetTransforms = versionedMap.map { case(version, (_, _, changeSet)) =>
        (changeSet.toLong, fullCollectionName(dbName, collectionName)) -> version.toDouble
      }
      requestTransforms ++= changeSetTransforms
      logDebug(s"Request Transforms = $changeSetTransforms")
    })
    logInfo(s"Completed Request Transforms generation for $transformType TransformType...")
    requestTransforms.toMap
  }

  private def generateResponseSnippets(transformType: TransformType, tree: Tree):
  Map[String, TreeMap[Double, BSONObject=>BSONObject]] = {
    logInfo(s"Started Response Snippets generation for $transformType TransformType...")
    val responseSnippets = scala.collection.mutable.Map[String, TreeMap[Double, BSONObject => BSONObject]]()
    tree.foreachDelta(transformType, { case (dbName: String, collectionName: String, versionedMap: MutableVersionedMap) =>
      val versionedSnippets = versionedMap.map { case(version, (verb, arguments, _)) =>
        val args = arguments.asScala.toArray.collect {
          case arg: String => arg
        }
        (version.toDouble -> toSnippet(verb, args))
      }
      val treeMap = scala.collection.immutable.TreeMap(versionedSnippets.toArray: _*)
      responseSnippets += (fullCollectionName(dbName, collectionName) -> treeMap)
      logDebug(s"Response Snippets = $responseSnippets")
    })
    logInfo(s"Completed Response Snippets generation for $transformType TransformType...")
    responseSnippets.toMap
  }
}
