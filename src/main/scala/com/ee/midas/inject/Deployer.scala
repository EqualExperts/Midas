package com.ee.midas.inject

object Deployer extends App {

  override def main(args: Array[String]): Unit = {
    val loader: ClassLoader = Thread.currentThread().getContextClassLoader()
    val classpathURI = "."
    val classpathDir = loader.getResource(classpathURI)
    println(s"classpathDir = $classpathDir")

    val binDirURI = "generated/scala/bin"
    val binDir = loader.getResource(binDirURI)
    println(s"output dir = $binDir")

    val srcScalaURI = "generated/scala/Transformations.scala"
    val srcScalaFile = loader.getResource(srcScalaURI)
    println(s"generated Scala File = $srcScalaFile")
    val compiler = new Compiler
    compiler.compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)
  }

}
