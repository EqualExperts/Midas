package com.ee.midas

import java.io.{PrintWriter, File}

package object config {
  def write(text: String, toFile: File) = {
    val writer = new PrintWriter(toFile, "utf-8")
    writer.write(text)
    writer.flush()
    writer.close()
  }
}
