/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.model

import java.net.ServerSocket
import scala.util.{Failure, Success, Try}

class ServerSetup {
  var midasServerPort = 0
  var mongoServerPort = 0
  var midasServer: ServerSocket = null
  var mongoServer: ServerSocket = null

  private def run(serverName: String, server: ServerSocket) = {
    new Thread(new Runnable {
      def run() = {
        while(!server.isClosed) {
          println(s"$serverName open on: ${server.getLocalPort}")
          Try {
            server.accept()
          } match {
            case Success(socket) => println("Connection Accepted")
            case Failure(t) => println(s"${t.getMessage}")
          }
        }
      }
    }, serverName).start()
  }

  def start = {
    println("BEFORE CLASS INVOKED. STARTING SERVERS")
    midasServer = new ServerSocket(0)
    midasServerPort = midasServer.getLocalPort
    mongoServer = new ServerSocket(0)
    mongoServerPort = mongoServer.getLocalPort
    run("Midas Server",midasServer)
    run("Mongo Server", mongoServer)
  }

  def stop = {
    println("AFTER CLASS INVOKED. SHUTTING DOWN SERVERS")
    midasServer.close()
    mongoServer.close()
  }
}
