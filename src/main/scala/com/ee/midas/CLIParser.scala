package com.ee.midas

import java.io.{File}
import java.net.{URI}
import com.ee.midas.utils.Loggable

case class MidasCmdConfig (val midasHost:String = "localhost",
                        val midasPort: Int = 27020 ,
                        val mongoHost: String = "localhost",
                        val mongoPort: Int = 27017,
                        val baseDeltasDir: URI)  {
}

object CLIParser extends Loggable {

  def parse(args:Array[String]): Option[MidasCmdConfig] = {
    val parser = new scopt.OptionParser[MidasCmdConfig]("midas") {
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
    val defaultMidasConfig = MidasCmdConfig(baseDeltasDir = baseDeltasDir)
    parser.parse(args, defaultMidasConfig)
  }
}
