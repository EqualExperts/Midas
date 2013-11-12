package com.ee.midas.utils

object Accumulator {
  def apply[T](initial: List[T]): T => List[T]= {
    var acc = initial

    (x: T) =>  x match {
      case Nil => acc
      case null => acc
      case _ => { acc = x :: acc
        acc
      }
    }
  }
}
