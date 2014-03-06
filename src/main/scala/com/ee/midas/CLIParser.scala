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

package com.ee.midas

import java.io.{File}
import java.net.{URL, URI}
import com.ee.midas.utils.Loggable

case class CmdConfig (val midasHost:String = "localhost",
                        val midasPort: Int = 27020 ,
                        val mongoHost: String = "localhost",
                        val mongoPort: Int = 27017,
                        val baseDeltasDir: URI)  {
}

object CLIParser extends Loggable {

  def parse(args:Array[String]): Option[CmdConfig] = {
    val parser = new scopt.OptionParser[CmdConfig]("midas") {
      opt[String]("host") action { (userSuppliedHost, defaultMidasConfig) =>
        defaultMidasConfig.copy(midasHost = userSuppliedHost)
      } text("OPTIONAL, the host/IP midas will be available on, default is localhost")
      opt[Int]("port") action { (userSuppliedPort, defaultMidasConfig) => 
        defaultMidasConfig.copy(midasPort = userSuppliedPort)
      } text("OPTIONAL, the port on which midas will accept connections, default is 27020")
      opt[String]("source") action { (userSuppliedSource, defaultMidasConfig) => 
        defaultMidasConfig.copy(mongoHost = userSuppliedSource)
      } text("OPTIONAL, the mongo host midas will connect to, default is localhost")
      opt[Int]("mongoPort") action { (userSuppliedMongoPort, defaultMidasConfig) => 
        defaultMidasConfig.copy(mongoPort = userSuppliedMongoPort)
      } text("OPTIONAL, the mongo port midas will connect to, default is 27017")

      def directoryExists: (File) => Either[String, Unit] = (userSuppliedDeltasDir) =>
        if (userSuppliedDeltasDir.exists())
          success
        else
          failure(s"${userSuppliedDeltasDir.getAbsolutePath} doesn't exist!")

      opt[File]("deltasDir") action {(userSuppliedDeltasDir, defaultMidasConfig) =>
        defaultMidasConfig.copy(baseDeltasDir = userSuppliedDeltasDir.toURI)
      } validate { directoryExists } text("OPTIONAL, the location of delta files ")
      help("help") text ("Show usage")
    }

    val loader = this.getClass.getClassLoader
    val baseDeltasDir = loader.getResource("deltas").toURI
    logInfo(s"Default Base DeltasDir = $baseDeltasDir")
    val defaultMidasConfig = CmdConfig(baseDeltasDir = baseDeltasDir)
    parser.parse(args, defaultMidasConfig)
  }
}
