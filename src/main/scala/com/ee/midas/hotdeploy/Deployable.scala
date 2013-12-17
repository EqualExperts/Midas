package com.ee.midas.hotdeploy

import com.ee.midas.utils.Loggable
import java.net.URL

trait Deployable extends Loggable {
  def deploy[T](parent: ClassLoader, loadFrom: Array[URL], clazzName: String): T = {
    try {
      val urlClassLoader = new ChildFirstClassLoader(loadFrom, parent)
      log.debug(s"Trying to Load and Instantiate Class = ${clazzName}...")
      val newInstance = urlClassLoader.loadClass(clazzName).newInstance().asInstanceOf[T]
      log.debug(s"Instantiated Class = ${clazzName}")
      newInstance
    } catch {
      case e: InstantiationException => throw new RuntimeException(e.getCause)
      case e: IllegalAccessException => throw new RuntimeException(e.getCause)
      case e: ClassNotFoundException => throw new RuntimeException(e.getCause)
    }
  }
}
