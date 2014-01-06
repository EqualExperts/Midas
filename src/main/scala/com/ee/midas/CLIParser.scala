package com.ee.midas

import com.ee.midas.transform.TransformType
import scopt.{Read, OptionParser}
import java.io.File

case class MidasConfig (midasHost:String = "localhost",
                        midasPort: Int = 27020 ,
                        mongoHost: String = "localhost",
                        mongoPort: Int = 27017,
                        mode: TransformType = TransformType.EXPANSION,
                        deltasDir: String = "deltas")

object CLIParser {
  implicit val transformTypRead: Read[TransformType] = new Read[TransformType]{
    def arity = 1
    def reads: String => TransformType = (mode: String) => TransformType.valueOf(mode.toUpperCase)
  }

  def parse(args:Array[String]): Option[MidasConfig] = {
    val parser = new scopt.OptionParser[MidasConfig]("midas") {
      opt[Int]("port") action { (userSuppliedPort, defaultMidasConfig) => 
        defaultMidasConfig.copy(midasPort = userSuppliedPort)
      } text("OPTIONAL, the port on which midas will accept connections, default is 27020")
      opt[String]("source") action { (userSuppliedSource, defaultMidasConfig) => 
        defaultMidasConfig.copy(mongoHost = userSuppliedSource)
      } text("OPTIONAL, the mongo host midas will connect to, default is localhost")
      opt[Int]("mongoPort") action { (userSuppliedMongoPort, defaultMidasConfig) => 
        defaultMidasConfig.copy(mongoPort = userSuppliedMongoPort)
      } text("OPTIONAL, the mongo port midas will connect to, default is 27017")
      opt[TransformType]("mode") action { (userSuppliedMode, defaultMidasConfig) =>
        defaultMidasConfig.copy(mode = userSuppliedMode)
      } text("OPTIONAL, the operation mode (EXPANSION/CONTRACTION) for midas, default is EXPANSION")
      opt[String]("deltasDir") action { (userSuppliedDeltasDir, defaultMidasConfig) =>
        deltasDir(userSuppliedDeltasDir, defaultMidasConfig, this)
      } text("OPTIONAL, the location of delta files ")
      help("help") text ("Show usage")
//      override def reportError(msg: String) : Unit = {
//        println(msg)
//      }
    }
    parser.parse(args, MidasConfig())
  }

  private def deltasDir(value: String, config: MidasConfig, parser: OptionParser[MidasConfig]) = {
    val file = new File(value)
    if (file.exists)
        config.copy(deltasDir = value)
    else {
        println("--deltasDir path does not exist")
        println(parser.usage)
        sys.exit
    }
  }
}
