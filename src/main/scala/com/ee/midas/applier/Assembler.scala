package com.ee.midas.applier

object Assembler {
  def assemble[T : Manifest](buffer1: Array[T], buffer2: Array[T]): Array[T] = {
    val newBuffer: Array[T]  = new Array[T](buffer1.length + buffer2.length)

    System.arraycopy(buffer1, 0, newBuffer, 0, buffer1.length)
    System.arraycopy(buffer2, 0, newBuffer, buffer1.length, buffer2.length)

    newBuffer
  }
}
