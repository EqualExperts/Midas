/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

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
