package com.ee.midas.config

import scala.util.parsing.combinator.JavaTokenParsers
import java.net.URL
import scala.util.Try
import java.io.File

/**
 * Example: midas.config
 * apps {
 *   app1
 *   app2
 * }
 *
 * BNF for Midas Configuration
 * --------------------------------------
 * apps ::=  "apps" "{" {appName} "}"
 * appName ::=  ident
 *
 */
trait ConfigurationParser extends JavaTokenParsers {

  final val configFilename = "midas.config"

  //Eat Java-Style comments like whitespace
  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r

  def configuration(deltasDir: URL): Parser[Configuration] = "apps" ~ "{" ~> rep(ident) <~ "}" ^^ { case appNames => Configuration(deltasDir, appNames) }

  def parse(input: String, deltasDir: URL): Configuration = parseAll(configuration(deltasDir), input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  def parse(deltasDir: URL): Try[Configuration] = Try {
    val midasConfig = new URL(deltasDir.toString + configFilename)
    val configText: String = scala.io.Source.fromURL(midasConfig).mkString
    parse(configText, deltasDir)
  }
}
