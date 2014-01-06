package com.ee.midas

import com.ee.midas.transform.TransformType
import scopt.OptionParser
import java.io.File

case class Config (midasHost:String = "localhost", midasPort: Int = 27020 , mongoHost: String = "localhost", mongoPort: Int = 27017, mode:TransformType = TransformType.EXPANSION, deltasDir: String = "deltas")

object CLIParser {
  def parse(args:Array[String]) = {

    val parser = new scopt.OptionParser[Config]("midas") {
      opt[Int]("port") action { (x, c) => c.copy(midasPort = x)
                              } text("OPTIONAL, the port on which midas will accept connections, default is 27020")
      opt[String]("source") action { (x, c) => c.copy(mongoHost = x)
                                   } text("OPTIONAL, the mongo host midas will connect to, default is localhost")
      opt[Int]("mongoPort") action { (x, c) => c.copy(mongoPort = x)
                                   } text("OPTIONAL, the mongo port midas will connect to, default is 27017")
      opt[String]("mode") action { (x, c) =>  userSuppliedMode(x, c, this)
                                 } text("OPTIONAL, the operation mode (EXPANSION/CONTRACTION) for midas, default is EXPANSION")
      opt[String]("deltasDir") action { (x, c) => userSuppliedDeltasDir(x, c, this)
                                      } text("OPTIONAL, the location of delta files ")
      help("help") text ("Show usage")
      override def reportError(msg: String) : Unit = {
        println(usage)
        sys.exit
      }
    }

    parser.parse(args, Config()) map { config =>
      (config.midasHost, config.midasPort, config.mongoHost, config.mongoPort, config.mode, config.deltasDir)
    } getOrElse {
      println("Terminating execution...!!")
    }

  }

  private def userSuppliedMode(value: String, config: Config, parser: OptionParser[Config]) = {
    try {
       config.copy(mode = TransformType.valueOf(value.toUpperCase))
    } catch {
       case e: IllegalArgumentException => println(parser.usage)
                                           sys.exit
    }
  }

  private def userSuppliedDeltasDir(value: String, config: Config, parser: OptionParser[Config]) = {
    val file: File = new File(value)
    if (file.exists)
        config.copy(deltasDir = value)
    else {
        println("--deltasDir path does not exist")
        println(parser.usage)
        sys.exit
    }
  }
}
