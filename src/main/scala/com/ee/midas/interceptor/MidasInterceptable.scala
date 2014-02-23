package com.ee.midas.interceptor

import com.ee.midas.pipes.Interceptable
import java.io.{InputStream, OutputStream}
import com.ee.midas.utils.Loggable

abstract class MidasInterceptable extends Interceptable with Loggable {

  override def intercept(src: InputStream, tgt: OutputStream): Int = {
    val header = readHeader(src)
    val inputData = read(src, header)
    write(inputData, tgt)
  }

  def write(data: Array[Byte], tgt: OutputStream): Int = {
    val bytesToWrite = data.length
    tgt.write(data, 0, bytesToWrite)
    tgt.flush()
    bytesToWrite
  }

  def read(src: InputStream, header: BaseMongoHeader) : Array[Byte]

  def readHeader(src: InputStream) : BaseMongoHeader
}
