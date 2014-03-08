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

package com.ee.midas.interceptor

import org.bson.io.Bits
import org.bson.BSONObject
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._
import scala.language.postfixOps

//todo: Design changes for later
// Request really needs to be composed of BaseMongoHeader and Transformer
// Current scenario is like anemic domain model where we have header and transformer
// both outside. RequestInterceptor, the client, co-ordinates header, sucks out info from
// request, transforms it, and puts it back in the request.
sealed trait Request {
  val CSTRING_TERMINATION_DELIM = 0
  val delimiterLength = 1

  def extractFullCollectionName(data: Array[Byte]): String = {
    val result : Array[Byte] = data.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
  }

  protected val payloadStartIndex: Int
  def extractPayload(data: Array[Byte]) = (data.take(payloadStartIndex), data.drop(payloadStartIndex))

  def extractDocument(): BSONObject
  def reassemble(modifiedDocument: BSONObject): Array[Byte]
}

case class Update(data: Array[Byte]) extends Request {
  private val updateFlagLength = 4
  override protected val payloadStartIndex = extractFullCollectionName(data).length + updateFlagLength + delimiterLength
  val (initialBytes, payload) = extractPayload(data)
  val (selector, updator) = getSelectorUpdator

  def getUpdateFlag(): Int = {
    val result = data.dropWhile(_ != CSTRING_TERMINATION_DELIM)
    val startPosIgnoringDelimiter = 1
    val flag = Bits.readInt(result, startPosIgnoringDelimiter)
    flag
  }

  private def getSelectorUpdator(): (BSONObject, BSONObject) = {
    val stream = new ByteArrayInputStream(payload)
    val selector: BSONObject = stream
    val updator: BSONObject = stream
    (selector, updator)
  }

  def extractDocument: BSONObject = updator

  def reassemble(modifiedDocument: BSONObject): Array[Byte] = {
    val modifiedPayload = selector.toBytes ++ modifiedDocument.toBytes
    initialBytes ++ modifiedPayload
  }
}

case class Insert(data: Array[Byte]) extends Request {
  override protected val payloadStartIndex = extractFullCollectionName(data).length + delimiterLength
  val (initialBytes, payload) = extractPayload(data)

  def extractDocument: BSONObject = payload

  def reassemble(modifiedDocument: BSONObject): Array[Byte] =
    initialBytes ++ modifiedDocument.toBytes
}
