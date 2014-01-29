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
