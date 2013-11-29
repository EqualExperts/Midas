package com.ee.midas.inject

import com.ee.midas.utils.Loggable
import com.ee.midas.transform.{Transformations, TransformationsHolder}

object Deployer extends Loggable with App {

  def deploy(loader: ClassLoader): Unit = {
    val clazzName = "com.ee.midas.transform.Transformations"
    log.info(s"Loading Class = ${clazzName}")
    val clazz = loader.loadClass(clazzName)
    log.info(s"Loaded Class = ${clazz.getName}")
    log.info(s"Instantiating Class = ${clazz.getName}")
    val newInstance = clazz.newInstance().asInstanceOf[Transformations]
    log.info(s"Instantiated Class = ${clazz.getName}")

    log.info(s"All Transformations BEFORE = $TransformationsHolder")
    val oldInstance = TransformationsHolder.get
    log.info(s"Old Instance = $oldInstance")
    TransformationsHolder.set(newInstance)
    val newlySetInstance = TransformationsHolder.get
    log.info(s"New Instance = $newlySetInstance")
    log.info(s"All Transformations AFTER = $TransformationsHolder")
  }

  override def main(args: Array[String]): Unit = {
    deploy(Thread.currentThread().getContextClassLoader)
  }
}
