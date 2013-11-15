package com.ee.midas.interceptor

import java.io.InputStream

/**
 * Code taken from mongo-driver and converted to scala.
 * @param in
 * @param limit
 */
class FixedSizeStream(in: InputStream, var limit: Int) extends InputStream {

  override def available(): Int = in.available()

  def read() : Int = {
    if(limit <= 0) -1
    else {
        val value: Int = in.read()
        limit -= 1
        value
    }
  }

  override def read(bytes: Array[Byte], off: Int, len: Int): Int = {
    if(limit <= 0) -1
    else {
        val bytesRead: Int = in.read(bytes, off, Math.min(limit, len))
        limit -= bytesRead
        bytesRead
    }
  }

  override def close() : Unit = throw new RuntimeException("can't close this")

}