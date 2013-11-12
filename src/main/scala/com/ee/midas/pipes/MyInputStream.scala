package com.ee.midas.pipes

import java.io.InputStream

class MyInputStream(val in: InputStream , var toGo: Int) extends InputStream {


  override def available(): Int = in.available()

  override def read(): Int = {

    if (toGo <= 0)
      -1

    val value : Int = in.read();
    toGo -= 1

    value
  }

  override def read( b : Array[Byte],off : Int, len :Int) : Int = {

    if (toGo <= 0)
      -1

    val n : Int = in.read(b, off, Math.min(toGo, len));
    toGo -= n
    n
  }

  override def close : Unit = throw new RuntimeException("can't close this")

}
