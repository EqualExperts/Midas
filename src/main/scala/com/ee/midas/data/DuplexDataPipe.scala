package com.ee.midas.data

class DuplexDataPipe(requestPipe: SimplexPipe, responsePipe: SimplexPipe) {

  def handleRequest(){
    requestPipe.handle()
  }

  def handleResponse(){
    responsePipe.handle()
  }
}
