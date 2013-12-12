package com.ee.midas.inject

import com.ee.midas.utils.Loggable
import com.ee.midas.transform.{Transforms, Transformations, TransformsHolder}
import java.net.{URLClassLoader, URL}

class MidasClassLoader(val urls: Array[URL], val parent: ClassLoader) extends URLClassLoader(urls, parent) with Loggable {

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    try {
      log.info(s"Asking Child for CLASS $name")
      findClass(name)
    } catch {
      case e: ClassNotFoundException =>
        // checking parent
        // This call to loadClass may eventually call findClass again, in case the parent doesn't find anything.
        log.info(s"OOPS! Child DID NOT Give CLASS $name, Getting from Parent")
        val clazz = super.loadClass(name, resolve)
        log.info(s"Returning from Parent $clazz")
        clazz
    }
  }
}


object Deployer extends Loggable with App {

  def deploy(loader: ClassLoader, loadFrom: Array[URL]): Unit = {
    val clazzName = "com.ee.midas.transform.Transformations"
    val urlClassLoader = new MidasClassLoader(loadFrom, loader)
//    val urlClassLoader = new URLClassLoader(loadFrom, loader) {
//      override def loadClass(name: String, resolve: Boolean): Class[_] = {
//        try {
//          checking local
//          log.info(s"Asking Child for CLASS $name")
//          findClass(name)
//        } catch {
//          case e: ClassNotFoundException =>
//            checking parent
//            This call to loadClass may eventually call findClass again, in case the parent doesn't find anything.
//            log.info(s"OOPS! Child DID NOT Give CLASS $name, Getting from Parent")
//            val clazz = super.loadClass(name, resolve)
//            log.info(s"Returning from Parent $clazz")
//            clazz
//        }
//      }
//    }

    try {
      log.info(s"Trying to Load and Instantiate Class = ${clazzName}...")
      val newInstance = urlClassLoader.loadClass(clazzName).newInstance()
      log.info(s"All Transformations BEFORE = $TransformsHolder")
      val oldInstance = TransformsHolder.get
      log.info(s"Old Instance = $oldInstance")
      TransformsHolder.set(newInstance.asInstanceOf[Transforms])
      val newlySetInstance = TransformsHolder.get
      log.info(s"New Instance = $newlySetInstance")
      log.info(s"All Transformations AFTER = $TransformsHolder")
    } catch {
      case e: InstantiationException => throw new RuntimeException(e.getCause)
      case e: IllegalAccessException  => throw new RuntimeException(e.getCause)
      case e: ClassNotFoundException => throw new RuntimeException(e.getCause)
    }
  }

  override def main(args: Array[String]): Unit = {
    val loader = Deployer.getClass.getClassLoader
    val binDirURI = "generated/scala/bin/"
    val binDir = loader.getResource(binDirURI)
    log.info(s"output dir = $binDir")

    deploy(loader, Array(binDir))
  }
}
