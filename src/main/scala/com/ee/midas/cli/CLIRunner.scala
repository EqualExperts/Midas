package com.ee.midas.cli

import com.ee.midas.Main

object CLIRunner extends App{

  override def main(args:Array[String]) = {
    var port = ""
    val parser = new scopt.OptionParser("scopt") {

       opt("port" , "port is the port on which midas is gonna run" , x => port = x)
    }

    if (parser.parse(args)) {
      println("in if")
      Main.main(Array("localhost","27020","localhost","27017"))
    }
    else {
      println(" in else ")
    }
  }
}
