package com.ee.midas.interceptor

import java.io.InputStream

class FixedSizeStream(in: InputStream, var limit: Int) extends InputStream {

  override def available(): Int = in.available()

  def read() : Int =
    limit <= 0 match {
      case true => -1
      case false => {
        val value: Int = in.read()
        limit -= 1
        value
      }
    }

  override def read(bytes: Array[Byte], off: Int, len: Int): Int =
    limit <= 0 match {
      case true => -1
      case false => {
        val bytesRead: Int = in.read(bytes, off, Math.min(limit, len))
        limit -= bytesRead
        bytesRead
      }
    }

  override def close() = throw new RuntimeException("can't close this")

}