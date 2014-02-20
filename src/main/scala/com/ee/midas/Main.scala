package com.ee.midas


import com.ee.midas.utils.Loggable


object Main extends App with Loggable  {

  override def main(args:Array[String]): Unit = {

    //Todo: tweak scala style rule so that we don't have to give types when declaring variables.
    CLIParser.parse(args) match {
      case Some(cmdConfig) =>
        val midas = new MidasServer(cmdConfig)
        midas.start

      case None =>
    }
  }

}
