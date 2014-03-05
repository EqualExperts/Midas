package com.ee.midas.fixtures

import java.net.Socket
import com.ee.midas.{CmdConfig, MidasServer, CLIParser}

object MidasUtils {

  var midasProcess: MidasAsThread = null

  def startMidas(args: String) = {
    val cmdConfig = CLIParser.parse(args.split(" ")) match {
      case Some(cmdConfig) => cmdConfig
      case None => null
    }

    if(cmdConfig == null)
      println("Midas cannot be started . Invalid command line arguments")
    else
    {
      midasProcess = new MidasAsThread(cmdConfig)

      midasProcess.start()
      while(!midasProcess.isRunning()) {
        Thread.sleep(1000)
      }
      true
    }
  }

  def stopMidas(midasPort: Int) = {
    midasProcess.stopThread()
    try {
      val fakeSocket = new Socket("127.0.0.1", midasPort)
      fakeSocket.wait(500)
    } catch {
      case e:Exception => println("midas is closed")
    }
    while(midasProcess.isRunning())
        Thread.sleep(100)
    !midasProcess.isRunning()
  }
}

class MidasAsThread(cmdConfig: CmdConfig) extends Thread {

  val midas = new MidasServer(cmdConfig)
  override def run() = {
    midas.start
  }

  def stopThread() = { midas.stop }

  def isRunning() = { midas.isActive }
}
