package com.ee.midas.utils


class Accumulator[T] private (private var seq: Seq[T]) {
  def apply(elem: T): Seq[T] = elem match {
    case elem : Seq[_] => seq
    case null => seq
    case _ => {
      seq = seq.+:(elem)
      seq
    }
  }
}

object Accumulator {
  def apply[T] = new Accumulator[T](Seq[T]())
}
