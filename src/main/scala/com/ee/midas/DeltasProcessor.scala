package com.ee.midas

import com.ee.midas.utils.{FileVisitor, Loggable}
import com.ee.midas.dsl.Translator
import com.ee.midas.transform.{TransformType, Transformer}
import java.net.URL
import java.nio.file.Paths
import java.util.regex.Pattern
import java.io.File
import scala.collection.JavaConverters._

trait DeltasProcessor extends Loggable {
  def processDeltas(translator: Translator[Transformer], transformType: TransformType, deltasDir: URL): Transformer = {
    logDebug(s"Translating Delta Files...in ${deltasDir} for $transformType TransformType")
    val startDir = Paths.get(deltasDir.toURI)
    val deltaFiles = new FileVisitor (startDir, Pattern.compile(".*\\.delta$")).visit.map(new File(_))
    logDebug(s"Delta Files $deltaFiles")
    val sortedDeltaFiles = deltaFiles.sortBy(f => f.getAbsolutePath)
    logInfo(s"Sorted Delta Files $sortedDeltaFiles")
    translator.translate(transformType, sortedDeltaFiles.asJava)
  }
}
