package com.ee.midas.utils

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException
import java.util.regex.Pattern

class FileVisitor (startDir: Path, collectFilesRegex: Pattern) extends Loggable {
  private val files = scala.collection.mutable.Set[String]()

  private val visitor = new SimpleFileVisitor[Path] {
    override def visitFile(path: Path, mainAtts: BasicFileAttributes) = {
      log.debug(s"Path = $path")
      val file = path.toAbsolutePath.toString
      val matcher = collectFilesRegex.matcher(file)
      if(matcher.matches) {
        files += file
      }
      FileVisitResult.CONTINUE
    }

    override def visitFileFailed(path: Path, exc: IOException) = {
      log.info(s"Continuing Scanning though visiting File has Failed for $path, Message ${exc.getMessage}")
      log.info(s"${exc.printStackTrace}")
      FileVisitResult.CONTINUE
    }
  }

  def visit = {
    files.clear
    Files.walkFileTree(startDir, visitor)
    files.toSet
  }
}
