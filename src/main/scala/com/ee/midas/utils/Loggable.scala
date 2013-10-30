package com.ee.midas.utils

import org.slf4j.LoggerFactory

trait Loggable {
   val log = LoggerFactory.getLogger(this.getClass.getName)
   def logFor[T](clazz: Class[T]) = LoggerFactory.getLogger(clazz)
}
