package com.ee.midas.hotdeploy

import com.ee.midas.utils.Loggable
import java.net.{URL, URLClassLoader}

class ChildFirstClassLoader (val urls: Array[URL], val parent: ClassLoader)
  extends URLClassLoader(urls, parent) with Loggable {
  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    try {
      logDebug(s"Asking Child for CLASS $name")
      findClass(name)
    } catch {
      case e: ClassNotFoundException =>
        logDebug(s"OOPS! Child DID NOT Give CLASS $name, Getting from Parent")

        val clazz = super.loadClass(name, resolve)

        logDebug(s"Returning from Parent $clazz")

        clazz
    }
  }
}
