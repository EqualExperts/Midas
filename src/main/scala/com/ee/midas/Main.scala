package com.ee.midas


import com.ee.midas.utils.Loggable


object Main extends App with Loggable  {

  override def main(args:Array[String]): Unit = {
    CLIParser.parse(args) match {
      case Some(cmdConfig) =>
        val server = new MidasServer(cmdConfig)
        setupShutdownHook(server)
        server.start
      case None =>
    }
  }

  private def setupShutdownHook(server: MidasServer) = sys.ShutdownHookThread {
    val forceStopMsg = "User Forced Stop on Midas...Closing Open Connections"
    logInfo(forceStopMsg)
    println(forceStopMsg)
    server.stop
    val shutdownMsg = "Midas Shutdown Complete!"
    logInfo(shutdownMsg)
    println(shutdownMsg)
  }
}
