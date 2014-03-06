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

import org.slf4j.LoggerFactory

trait Loggable {
  private var log = LoggerFactory.getLogger(this.getClass.getName)
  def logFor[T](clazz: Class[T]): Unit = log = LoggerFactory.getLogger(clazz)

  def logInfo(msg: String) = if(log.isInfoEnabled) log.info(msg)
  def logInfo(msg: String, t: Throwable) = if(log.isInfoEnabled) log.info(msg, t)

  def logDebug(msg: String) = if(log.isDebugEnabled) log.debug(msg)
  def logDebug(msg: String, t: Throwable) = if(log.isDebugEnabled) log.debug(msg, t)

  def logError(msg: String) = if(log.isErrorEnabled) log.error(msg)
  def logError(msg: String, t: Throwable) = if(log.isErrorEnabled) log.error(msg, t)

  def logTrace(msg: String) = if(log.isTraceEnabled) log.trace(msg)
  def logTrace(msg: String, t: Throwable) = if(log.isTraceEnabled) log.trace(msg, t)

  def logWarn(msg: String) = if(log.isWarnEnabled) log.warn(msg)
  def logWarn(msg: String, t: Throwable) = if(log.isWarnEnabled) log.warn(msg, t)
}
