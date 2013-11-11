package com.ee.midas.utils

object Accumulator {
  def apply[T](initial: List[T]): T => List[T]= {
    var acc = initial
    (x: T) => {
      acc = if(x == null) acc else x :: acc
      acc
    }
  }
}
