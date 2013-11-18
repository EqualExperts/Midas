package com.ee.midas.pipes

import com.ee.midas.utils.Loggable
import java.io.{OutputStream, InputStream}

trait Interceptable extends Loggable {
  /**
   *  Default Interception does nothing, simply copies data from input to output.
   * @param src InputStream
   * @param tgt OutputStream
   * @return number of bytes read or None for EOF (End of stream).
   */
  def intercept(src: InputStream, tgt: OutputStream): Int = {
    val name = getClass.getName
    var bytesRead = 0
    val data = new Array[Byte](1024 * 16)
    bytesRead = src.read(data)
    log.info(name + " Bytes Read = " + bytesRead)
    if (bytesRead > 0) {
      tgt.write(data, 0, bytesRead)
      log.info(name + " Bytes Written = " + bytesRead)
      tgt.flush
    }
    bytesRead
  }
}

object Interceptable {
  def apply() = new Interceptable {}
}