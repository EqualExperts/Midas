package com.ee.midas.interceptor

import com.ee.midas.pipes.Interceptable
import java.io.{InputStream, OutputStream}

trait MidasInterceptable extends Interceptable {
  override def intercept(src: InputStream, tgt: OutputStream): Int = {
    val inputData = read(src)
    write(inputData, tgt)
  }

  def write(data: Array[Byte], tgt: OutputStream): Int = {
    val bytesToWrite = data.length
    tgt.write(data, 0, bytesToWrite)
    tgt.flush()
    bytesToWrite
  }
  
  def read(src: InputStream) : Array[Byte]
}
