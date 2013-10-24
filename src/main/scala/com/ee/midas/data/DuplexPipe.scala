package com.ee.midas.data

import java.io.{OutputStream, InputStream}

class DuplexPipe(sourceInputStream: InputStream, sourceOutputStream: OutputStream,
                 destInputStream: InputStream, destOutputStream: OutputStream) {

  val requestPipe = new SimplexPipe(sourceInputStream, destOutputStream)
  val responsePipe = new SimplexPipe(destInputStream, sourceOutputStream)

  def transferData() = {
    requestPipe.start()
    responsePipe.start()
   }

  def waitForClientToTerminate() {
    requestPipe.join()
  }

}

