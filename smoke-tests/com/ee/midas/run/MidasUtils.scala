package com.ee.midas.run

import java.net.Socket

case object MidasUtils {

  var midasProcess: MidasAsThread = null

  def startMidas(args: String) = {
    midasProcess = new MidasAsThread(args.split(" "))
    midasProcess.start()
    println("Issues start to midas... waiting for midas to get up...")
    while(!midasProcess.isRunning()) {
      Thread.sleep(1000)
    }
    println("Midas woke up... returning control")
    true
  }

  def stopMidas(midasPort: Int) = {
    midasProcess.stopThread()
    println("creating fake socket to stop midas")
    try {
      val fakeSocket = new Socket("localhost", midasPort)
      fakeSocket.wait(500)
    } catch {
      case e:Exception => println("midas is closed")
    }
    println("Faking done... sleeping")
    Thread.sleep(100)
    !midasProcess.isRunning()
  }
}

class MidasAsThread(args: Array[String] = Array()) extends Thread {

  val midas = com.ee.midas.Main
  override def run() = {
    midas.main(args)
  }

  def stopThread() = { midas.stop }

  def isRunning() = { midas.isRunning }
}
