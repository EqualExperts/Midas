package com.ee.midas.data

import java.net.Socket

class DuplexChannel(midasClient: Socket, targetMongo: Socket) extends Thread {

  override def run() = {
    handleData()
    closeConnection()
  }

  var pipeFactory: PipeFactory = new PipeFactory()


  def handleData() = {
    val requestPipe = pipeFactory.createSimplexPipe(midasClient, targetMongo)
    val responsePipe = pipeFactory.createSimplexPipe(targetMongo, midasClient)

    requestPipe.start()
    responsePipe.start()
    requestPipe.join()
    println("join completed")
  }

  def closeConnection() = {
    println("shutting down connection")
    midasClient.close()
    targetMongo.close()
  }
}

