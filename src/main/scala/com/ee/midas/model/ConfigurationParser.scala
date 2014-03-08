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

  //Eat Java-Style comments like whitespace
  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r

  def configuration(deltasDir: URL): Parser[Configuration] = "apps" ~ "{" ~> rep(ident) <~ "}" ^^ { case appNames => new Configuration(deltasDir, appNames) }

  def parse(input: String, deltasDir: URL): Configuration = parseAll(configuration(deltasDir), input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  def parse(deltasDir: URL, configFilename: String): Try[Configuration] = Try {
    val midasConfig = new URL(deltasDir.toString + configFilename)
    val configText: String = scala.io.Source.fromURL(midasConfig).mkString
    parse(configText, deltasDir)
  }
}
