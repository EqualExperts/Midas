package com.ee.midas.hotdeploy

import com.ee.midas.utils.Loggable
import com.ee.midas.transform.{Transforms, Transformations, TransformsHolder}
import java.net.{URLClassLoader, URL}

trait Deployable extends Loggable {
  def deploy(parent: ClassLoader, loadFrom: Array[URL], clazzName: String): Transforms = {
    try {
      val urlClassLoader = new ChildFirstClassLoader(loadFrom, parent)
      log.info(s"Trying to Load and Instantiate Class = ${clazzName}...")
      urlClassLoader.loadClass(clazzName).newInstance().asInstanceOf[Transforms]
    } catch {
      case e: InstantiationException => throw new RuntimeException(e.getCause)
      case e: IllegalAccessException  => throw new RuntimeException(e.getCause)
      case e: ClassNotFoundException => throw new RuntimeException(e.getCause)
    }
  }
}
