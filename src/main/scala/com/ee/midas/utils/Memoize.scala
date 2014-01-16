package com.ee.midas.utils

import scala.collection.mutable.Map

class Memoize[-T1, +R] private (fn: T1 => R) extends (T1 => R) {
  /**
   * The private[this] is really needed here, because Map[A, B] has invariant types.
   *
   * Chapter 19: Programming in Scala:
   * Section 19.7
   * You might wonder whether this code passes the Scala type checker. After all,
   * queues now contain two reassignable fields of the covariant parameter type T.
   * Is this not a violation of the variance rules? It would be indeed, except for
   * the detail that leading and trailing have a private[this] modifier and are thus
   * declared to be object private.
   *
   * As mentioned in Section 13.4, object private members can be accessed only from within
   * the object in which they are defined. It turns out that accesses to variables from
   * the same object in which they are defined do not cause problems with variance. The
   * intuitive explanation is that, in order to construct a case where variance would
   * lead to type errors, you need to have a reference to a containing object that has a
   * statically weaker type than the type the object was defined with. For accesses to
   * object private values, however, this is impossible.
   *
   * Scala's variance checking rules contain a special case for object private definitions.
   * Such definitions are omitted when it is checked that a type parameter with
   * either a + or - annotation occurs only in positions that have the same variance
   * classification. Therefore, the code below compiles without error.
   *
   * On the other hand, if you had left out the [this] qualifiers from the two private
   * modifiers, you would see two type errors:
   *
   * 1) contravariant type T1 occurs in invariant position in type =>
   * scala.collection.mutable.Map[T1,R] of value values
   *     private val values = Map[T1, R]()
   *
   * 2) covariant type R occurs in invariant position in type =>
   * scala.collection.mutable.Map[T1,R] of value values
   *    private val values = Map[T1, R]()
   */
  private[this] val values = Map[T1, R]()

  def apply(arg: T1): R = {
    if(values.contains(arg)) {
      values(arg)
    } else {
      val result = fn(arg)
      values += ((arg, result))
      result
    }
  }
}

object Memoize {
  def apply[T1, R](fn: T1 => R) = new Memoize[T1, R](fn)
}
