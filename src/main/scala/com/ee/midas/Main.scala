package com.ee.midas


import com.ee.midas.utils.Loggable


object Main extends App with Loggable  {

  override def main(args:Array[String]): Unit = {
    CLIParser.parse(args) match {
      case Some(cmdConfig) => new MidasServer(cmdConfig).start
      case None =>
    }
  }
}
